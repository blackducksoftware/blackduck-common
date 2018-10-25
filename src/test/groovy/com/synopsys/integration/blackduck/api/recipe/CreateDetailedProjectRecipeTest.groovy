package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.service.HubService
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.test.annotation.IntegrationTest
import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category

import static org.junit.Assert.assertEquals

@Category(IntegrationTest.class)
class CreateDetailedProjectRecipeTest extends BasicRecipe {
    @Test
    void testCreatingAProject() {
        /*
         * let's create the project/version in the Hub
         */
        ProjectRequest projectRequest = createProjectRequest(PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()
        String projectUrl = projectService.createProject(projectRequest)

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the HubService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        HubService hubService = hubServicesFactory.createHubService()
        ProjectView projectView = hubService.getResponse(projectUrl, ProjectView.class)
        ProjectVersionView projectVersionView = hubService.getResponse(projectView, ProjectView.CANONICALVERSION_LINK_RESPONSE)

        assertEquals(PROJECT_NAME, projectView.name)
        assertEquals('A sample testing project to demonstrate hub-common capabilities.', projectView.description)

        assertEquals(PROJECT_VERSION_NAME, projectVersionView.versionName)
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.phase)
        assertEquals(ProjectVersionDistributionType.OPENSOURCE, projectVersionView.distribution)
    }

    @After
    void cleanup() {
        def projectService = hubServicesFactory.createProjectService()
        Optional<ProjectView> createdProject = projectService.getProjectByName(PROJECT_NAME)
        if (createdProject.isPresent()) {
            projectService.deleteProject(createdProject.get())
        }
    }

}
