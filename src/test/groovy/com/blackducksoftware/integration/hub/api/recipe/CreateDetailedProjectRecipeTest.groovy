package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.*

import org.junit.After
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.IntegrationTest
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.service.HubService
import com.blackducksoftware.integration.hub.service.ProjectService

@Category(IntegrationTest.class)
class CreateDetailedProjectRecipeTest extends BasicRecipe {
    @Test
    void testCreatingAProject() {
        /*
         * let's create the project/version in the Hub
         */
        ProjectRequest projectRequest = createProjectRequest(PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectService projectService = hubServicesFactory.createProjectService()
        String projectUrl = projectService.createHubProject(projectRequest)

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the HubService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        HubService hubService = hubServicesFactory.createHubService()
        ProjectView projectView = hubService.getResponse(projectUrl, ProjectView.class)
        ProjectVersionView projectVersionView = hubService.getResponseFromLinkResponse(projectView, ProjectView.CANONICALVERSION_LINK_RESPONSE)

        assertEquals(PROJECT_NAME, projectView.name)
        assertEquals('A sample testing project to demonstrate hub-common capabilities.', projectView.description)

        assertEquals(PROJECT_VERSION_NAME, projectVersionView.versionName)
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.phase)
        assertEquals(ProjectVersionDistributionType.OPENSOURCE, projectVersionView.distribution)
    }


    @After
    void cleanup() {
        def projectService = hubServicesFactory.createProjectService()
        ProjectView createdProject = projectService.getProjectByName(PROJECT_NAME)
        projectService.deleteHubProject(createdProject)
    }
}
