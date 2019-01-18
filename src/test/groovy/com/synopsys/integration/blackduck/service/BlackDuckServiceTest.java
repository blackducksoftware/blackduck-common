package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckServiceTest {
    @Test
    public void testGettingResponseWhenLinkNotPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_not_complete.json");

        String incompleteJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionViewWithMissingLink = blackDuckJsonTransformer.getResponseAs(incompleteJson, ProjectVersionView.class);

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        BlackDuckService blackDuckService = new BlackDuckService(logger, blackDuckHttpClient, gson, objectMapper);

        Optional<VersionBomPolicyStatusView> versionBomPolicyStatusView = blackDuckService.getResponse(projectVersionViewWithMissingLink, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertFalse(versionBomPolicyStatusView.isPresent());
    }

    @Test
    public void testGettingResponseWhenLinkPresent() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_complete.json");

        String completeJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ProjectVersionView projectVersionView = blackDuckJsonTransformer.getResponseAs(completeJson, ProjectVersionView.class);

        InputStream responseInputStream = getClass().getResourceAsStream("/json/VersionBomPolicyStatusView.json");
        String responseContentString = IOUtils.toString(responseInputStream, StandardCharsets.UTF_8);
        Response mockedResponse = Mockito.mock(Response.class);
        Mockito.when(mockedResponse.getContentString()).thenReturn(responseContentString);

        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckHttpClient.execute(Mockito.any(Request.class))).thenReturn(mockedResponse);

        BlackDuckService blackDuckService = new BlackDuckService(logger, blackDuckHttpClient, gson, objectMapper);

        Optional<VersionBomPolicyStatusView> versionBomPolicyStatusView = blackDuckService.getResponse(projectVersionView, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertTrue(versionBomPolicyStatusView.isPresent());
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, versionBomPolicyStatusView.get().getOverallStatus());
    }

}
