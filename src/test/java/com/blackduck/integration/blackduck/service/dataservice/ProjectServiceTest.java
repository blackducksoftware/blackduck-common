package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectServiceTest {
    @Disabled
    @Test
    public void testGettingLatestProjectVersion() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
        InputStream inputStream = getClass().getResourceAsStream("/json/pageOfProjectVersionViews.json");

        String pageJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        BlackDuckPageResponse<ProjectVersionView> pageOfProjectVersionViews = blackDuckJsonTransformer.getResponses(pageJson, ProjectVersionView.class);
        List<ProjectVersionView> projectVersionViews = pageOfProjectVersionViews.getItems();

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        ProjectGetService projectGetService = Mockito.mock(ProjectGetService.class);

        ApiDiscovery apiDiscovery = new ApiDiscovery(new HttpUrl("https://blackduck.com"));
        ProjectService projectService = new ProjectService(blackDuckApiClient, apiDiscovery, logger, projectGetService);

        ProjectView projectView = new ProjectView();
        projectView.setName("unit test");

        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(projectView.metaVersionsLink()))).thenReturn(projectVersionViews);

        Optional<ProjectVersionView> projectVersionView = projectService.getNewestProjectVersion(projectView);
        assertTrue(projectVersionView.isPresent());
        assertEquals("dockertar", projectVersionView.get().getVersionName());
    }

}
