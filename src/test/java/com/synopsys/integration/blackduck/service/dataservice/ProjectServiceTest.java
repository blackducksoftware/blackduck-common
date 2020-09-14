package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

public class ProjectServiceTest {
    @Test
    public void testGettingLatestProjectVersion() throws IOException, IntegrationException {
        IntLogger logger = new BufferedIntLogger();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        RequestFactory requestFactory = BlackDuckServicesFactory.createDefaultRequestFactory();
        InputStream inputStream = getClass().getResourceAsStream("/json/pageOfProjectVersionViews.json");

        String pageJson = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        BlackDuckPageResponse<ProjectVersionView> pageOfProjectVersionViews = blackDuckJsonTransformer.getResponses(pageJson, ProjectVersionView.class);
        List<ProjectVersionView> projectVersionViews = pageOfProjectVersionViews.getItems();

        BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        ProjectGetService projectGetService = Mockito.mock(ProjectGetService.class);

        ProjectService projectService = new ProjectService(blackDuckService, requestFactory, logger, projectGetService);

        ProjectView projectView = new ProjectView();
        projectView.setName("unit test");

        Mockito.when(blackDuckService.getAllResponses(Mockito.eq(projectView), Mockito.eq(ProjectView.VERSIONS_LINK_RESPONSE))).thenReturn(projectVersionViews);

        Optional<ProjectVersionView> projectVersionView = projectService.getNewestProjectVersion(projectView);
        assertTrue(projectVersionView.isPresent());
        assertEquals("dockertar", projectVersionView.get().getVersionName());
    }

}
