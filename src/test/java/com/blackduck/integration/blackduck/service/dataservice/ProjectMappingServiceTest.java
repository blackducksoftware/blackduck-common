package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.manual.view.ProjectMappingView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@ExtendWith(TimingExtension.class)
class ProjectMappingServiceTest {
    private ProjectMappingService projectMappingService;
    private ProjectView projectView;
    private BlackDuckServicesFactory blackDuckServicesFactory;

    @BeforeEach
    void setUp() throws IntegrationException {
        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
        blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        projectMappingService = blackDuckServicesFactory.createProjectMappingService();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        String testProjectName = intHttpClientTestHelper.getProperty("TEST_PROJECT");
        String testProjectVersion = intHttpClientTestHelper.getProperty("TEST_VERSION");

        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(testProjectName, testProjectVersion);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);
        projectView = projectVersionWrapper.getProjectView();
    }

    @AfterEach
    void tearDown() throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        blackDuckApiClient.delete(projectView);
    }

    @Test
    void populateApplicationId() throws IntegrationException {
        List<ProjectMappingView> projectMappings = projectMappingService.getProjectMappings(projectView);
        assertEquals(0, projectMappings.size());

        // Testing Creation
        projectMappingService.populateApplicationId(projectView, "testApplicationId1");
        projectMappings = projectMappingService.getProjectMappings(projectView);
        assertEquals(1, projectMappings.size());

        ProjectMappingView projectMappingView = projectMappings.get(0);
        String applicationId = projectMappingView.getApplicationId();
        assertEquals("testApplicationId1", applicationId);

        // Testing Updating
        projectMappingService.populateApplicationId(projectView, "testApplicationId2");
        projectMappings = projectMappingService.getProjectMappings(projectView);
        assertEquals(1, projectMappings.size());

        projectMappingView = projectMappings.get(0);
        applicationId = projectMappingView.getApplicationId();
        assertEquals("testApplicationId2", applicationId);
    }

    @Test
    void getProjectMappings() throws IntegrationException {
        List<ProjectMappingView> projectMappings = projectMappingService.getProjectMappings(projectView);
        assertEquals(0, projectMappings.size());
    }

}
