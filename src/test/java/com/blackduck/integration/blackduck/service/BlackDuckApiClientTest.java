package com.blackduck.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.blackduck.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.blackduck.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.blackduck.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.response.Response;

@ExtendWith(TimingExtension.class)
public class BlackDuckApiClientTest {
    @Test
    public void testGettingResponseWhenLinkNotPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_not_complete.json");

        String incompleteJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionViewWithMissingLink = blackDuckJsonTransformer.getResponseAs(incompleteJson, ProjectVersionView.class);

        BlackDuckApiClient blackDuckApiClient = new BlackDuckApiClient(blackDuckHttpClient, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer);

        try {
            blackDuckApiClient.getResponse(projectVersionViewWithMissingLink.metaPolicyStatusLink());
            fail();
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains(ProjectVersionView.POLICY_STATUS_LINK));
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void testGettingResponseWhenLinkPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_complete.json");

        String completeJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionView = blackDuckJsonTransformer.getResponseAs(completeJson, ProjectVersionView.class);

        InputStream responseInputStream = getClass().getResourceAsStream("/json/VersionBomPolicyStatusView.json");
        String responseContentString = IOUtils.toString(responseInputStream, StandardCharsets.UTF_8);
        Response mockedResponse = Mockito.mock(Response.class);
        Mockito.when(mockedResponse.getContentString()).thenReturn(responseContentString);

        Mockito.when(blackDuckHttpClient.execute(Mockito.any(BlackDuckRequest.class))).thenReturn(mockedResponse);

        BlackDuckApiClient blackDuckApiClient = new BlackDuckApiClient(blackDuckHttpClient, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer);

        ProjectVersionPolicyStatusView projectVersionPolicyStatusView = blackDuckApiClient.getResponse(projectVersionView.metaPolicyStatusLink());
        assertEquals(ProjectVersionComponentPolicyStatusType.IN_VIOLATION, projectVersionPolicyStatusView.getOverallStatus());
    }

}
