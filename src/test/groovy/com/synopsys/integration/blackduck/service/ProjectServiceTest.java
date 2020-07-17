package com.synopsys.integration.blackduck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.service.json.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.service.json.BlackDuckPageResponse;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import org.apache.commons.io.IOUtils;
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
    @Test
    public void testGettingLatestProjectVersion() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        InputStream inputStream = getClass().getResourceAsStream("/json/pageOfProjectVersionViews.json");

        String pageJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        BlackDuckPageResponse<ProjectVersionView> pageOfProjectVersionViews = blackDuckJsonTransformer.getResponses(pageJson, ProjectVersionView.class);
        List<ProjectVersionView> projectVersionViews = pageOfProjectVersionViews.getItems();

        BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        ProjectGetService projectGetService = Mockito.mock(ProjectGetService.class);

        ProjectService projectService = new ProjectService(blackDuckService, logger, projectGetService);

        ProjectView projectView = new ProjectView();
        projectView.setName("unit test");

        Mockito.when(blackDuckService.getAllResponses(Mockito.eq(projectView), Mockito.eq(ProjectView.VERSIONS_LINK_RESPONSE))).thenReturn(projectVersionViews);

        Optional<ProjectVersionView> projectVersionView = projectService.getNewestProjectVersion(projectView);
        assertTrue(projectVersionView.isPresent());
        assertEquals("dockertar", projectVersionView.get().getVersionName());
    }

}
