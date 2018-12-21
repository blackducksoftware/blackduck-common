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
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckServiceTest {
    @Test
    public void testGettingResponseWhenLinkNotPresent() throws IOException, IntegrationException {
        final IntLogger logger = new BufferedIntLogger();
        final Gson gson = BlackDuckServicesFactory.createDefaultGson();
        final ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        final BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        final InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_not_complete.json");

        final String incompleteJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        final ProjectVersionView projectVersionViewWithMissingLink = blackDuckJsonTransformer.getResponseAs(incompleteJson, ProjectVersionView.class);

        final BlackDuckRestConnection blackDuckRestConnection = Mockito.mock(BlackDuckRestConnection.class);
        final BlackDuckService blackDuckService = new BlackDuckService(logger, blackDuckRestConnection, gson, objectMapper);

        final Optional<VersionBomPolicyStatusView> versionBomPolicyStatusView = blackDuckService.getResponse(projectVersionViewWithMissingLink, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertFalse(versionBomPolicyStatusView.isPresent());
    }

    @Test
    public void testGettingResponseWhenLinkPresent() throws IOException, IntegrationException {
        final IntLogger logger = new BufferedIntLogger();
        final Gson gson = BlackDuckServicesFactory.createDefaultGson();
        final ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        final BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        final InputStream inputStream = getClass().getResourceAsStream("/json/ProjectVersionView_complete.json");

        final String completeJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        final ProjectVersionView projectVersionView = blackDuckJsonTransformer.getResponseAs(completeJson, ProjectVersionView.class);

        final InputStream responseInputStream = getClass().getResourceAsStream("/json/VersionBomPolicyStatusView.json");
        final String responseContentString = IOUtils.toString(responseInputStream, StandardCharsets.UTF_8);
        final Response mockedResponse = Mockito.mock(Response.class);
        Mockito.when(mockedResponse.getContentString()).thenReturn(responseContentString);

        final BlackDuckRestConnection blackDuckRestConnection = Mockito.mock(BlackDuckRestConnection.class);
        Mockito.when(blackDuckRestConnection.execute(Mockito.any(Request.class))).thenReturn(mockedResponse);

        final BlackDuckService blackDuckService = new BlackDuckService(logger, blackDuckRestConnection, gson, objectMapper);

        final Optional<VersionBomPolicyStatusView> versionBomPolicyStatusView = blackDuckService.getResponse(projectVersionView, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
        assertTrue(versionBomPolicyStatusView.isPresent());
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, versionBomPolicyStatusView.get().getOverallStatus());
    }

}
