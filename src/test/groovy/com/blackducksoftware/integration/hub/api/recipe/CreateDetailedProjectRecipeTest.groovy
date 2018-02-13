package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.*

import org.junit.After
import org.junit.AfterClass
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.IntegrationTest
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.dataservice.HubDataService
import com.blackducksoftware.integration.hub.dataservice.ProjectDataService
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder

@Category(IntegrationTest.class)
class CreateDetailedProjectRecipeTest extends BasicRecipe {
    @Test
    void testCreatingAProject() {
        /*
         * let's create the project/version in the Hub
         */
        ProjectRequest projectRequest = createProjectRequest(PROJECT_NAME, PROJECT_VERSION_NAME)
        ProjectDataService projectDataService = hubDataServicesFactory.createProjectDataService()
        String projectUrl = projectDataService.createHubProject(projectRequest)

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the HubService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        HubDataService hubService = hubDataServicesFactory.createHubDataService()
        ProjectView projectView = hubService.getResponse(projectUrl, ProjectView.class)
        ProjectVersionView projectVersionView = hubService.getResponseFromLinkResponse(projectView, ProjectView.CANONICALVERSION_LINK_RESPONSE)

        assertEquals(PROJECT_NAME, projectView.name)
        assertEquals('A sample testing project to demonstrate hub-common capabilities.', projectView.description)

        assertEquals(PROJECT_VERSION_NAME, projectVersionView.versionName)
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.phase)
        assertEquals(ProjectVersionDistributionType.OPENSOURCE, projectVersionView.distribution)
    }

    @After
    private ProjectRequest createProjectRequest() {
        /*
         * the ProjectRequestBuilder is a simple wrapper around creating a
         * ProjectRequest that will also include a ProjectVersionRequest to
         * create both a project in the Hub and a version for that created
         * project - a project must have at least one version
         */
        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.projectName = PROJECT_NAME
        projectRequestBuilder.description = 'A sample testing project to demonstrate hub-common capabilities.'
        projectRequestBuilder.versionName = PROJECT_VERSION_NAME
        projectRequestBuilder.phase = ProjectVersionPhaseType.DEVELOPMENT.name()
        projectRequestBuilder.distribution = ProjectVersionDistributionType.OPENSOURCE.name()

        projectRequestBuilder.build()
    }

    @AfterClass
    static void cleanup() {
        def hubServicesFactory = restConnectionTestHelper.createHubDataServicesFactory()
        def projectDataService = hubServicesFactory.createProjectDataService()
        ProjectView createdProject = projectDataService.getProjectByName(PROJECT_NAME)
        projectDataService.deleteHubProject(createdProject)
    }
}
