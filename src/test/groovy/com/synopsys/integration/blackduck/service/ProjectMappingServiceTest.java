package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.ProjectMappingView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
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
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        blackDuckService.delete(projectView);
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