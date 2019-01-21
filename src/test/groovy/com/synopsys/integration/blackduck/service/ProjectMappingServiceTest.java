package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.core.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.api.generated.view.ProjectMappingView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
class ProjectMappingServiceTest {
    private ProjectMappingService projectMappingService;
    private ProjectView projectView;
    private BlackDuckServicesFactory blackDuckServicesFactory;

    @BeforeEach
    void setUp() throws IntegrationException {
        final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
        blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        projectMappingService = blackDuckServicesFactory.createProjectMappingService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();
        final String testProjectName = intHttpClientTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersion = intHttpClientTestHelper.getProperty("TEST_VERSION");

        final Optional<ProjectView> existingProjectView = projectService.getProjectByName(testProjectName);
        if (existingProjectView.isPresent()) {
            final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
            blackDuckService.delete(existingProjectView.get());
        }

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder(testProjectName, testProjectVersion);
        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequestBuilder.build());
        projectView = projectVersionWrapper.getProjectView();
    }

    @AfterEach
    void tearDown() throws IntegrationException {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
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
        final List<ProjectMappingView> projectMappings = projectMappingService.getProjectMappings(projectView);
        assertEquals(0, projectMappings.size());
    }
}