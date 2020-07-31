package com.synopsys.integration.blackduck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.UrlSupport;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
public class BlackDuckServiceTest {
    @Test
    public void testGettingResponseWhenLinkNotPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        RequestFactory requestFactory = BlackDuckServicesFactory.createDefaultRequestFactory();
        UrlSupport urlSupport = BlackDuckServicesFactory.createDefaultUrlSupport();
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_not_complete.json");

        String incompleteJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionViewWithMissingLink = blackDuckJsonTransformer.getResponseAs(incompleteJson, ProjectVersionView.class);

        BlackDuckService blackDuckService = new BlackDuckService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, requestFactory, urlSupport);

        Optional<ProjectVersionPolicyStatusView> ProjectVersionPolicyStatusView = blackDuckService.getResponse(projectVersionViewWithMissingLink, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertFalse(ProjectVersionPolicyStatusView.isPresent());
    }

    @Test
    public void testGettingResponseWhenLinkPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        BlackDuckResponseTransformer blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        RequestFactory requestFactory = BlackDuckServicesFactory.createDefaultRequestFactory();
        UrlSupport urlSupport = BlackDuckServicesFactory.createDefaultUrlSupport();
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_complete.json");

        String completeJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionView = blackDuckJsonTransformer.getResponseAs(completeJson, ProjectVersionView.class);

        InputStream responseInputStream = getClass().getResourceAsStream("/json/VersionBomPolicyStatusView.json");
        String responseContentString = IOUtils.toString(responseInputStream, StandardCharsets.UTF_8);
        Response mockedResponse = Mockito.mock(Response.class);
        Mockito.when(mockedResponse.getContentString()).thenReturn(responseContentString);

        Mockito.when(blackDuckHttpClient.execute(Mockito.any(Request.class))).thenReturn(mockedResponse);

        BlackDuckService blackDuckService = new BlackDuckService(blackDuckHttpClient, gson, blackDuckJsonTransformer, blackDuckResponseTransformer, blackDuckResponsesTransformer, requestFactory, urlSupport);

        Optional<ProjectVersionPolicyStatusView> ProjectVersionPolicyStatusView = blackDuckService.getResponse(projectVersionView, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertTrue(ProjectVersionPolicyStatusView.isPresent());
        assertEquals(PolicyStatusType.IN_VIOLATION, ProjectVersionPolicyStatusView.get().getOverallStatus());
    }

}
