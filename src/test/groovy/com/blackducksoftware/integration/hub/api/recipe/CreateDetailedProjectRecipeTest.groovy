package com.blackducksoftware.integration.hub.api.recipe

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.IntegrationTest
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService
import com.blackducksoftware.integration.hub.global.HubServerConfig
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.service.HubService
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.test.TestLogger

@Category(IntegrationTest.class)
class CreateDetailedProjectRecipeTest {
    static final String PROJECT_NAME = 'My Recipe Project'
    static final String PROJECT_VERSION_NAME = '0.0.1-SNAPSHOT'

    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    void testCreatingAProject() {
        /*
         * the integration logger used to display log messages from our code
         * within a 3rd party integration environment
         */
        IntLogger intLogger = new TestLogger()

        /*
         * any usage of the Hub API's has to begin with a url for the hub
         * server and either:
         *   an API key
         * --or--
         *   a username and a password (this is what we're using here)
         */
        HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig()

        /*
         * next, we need to create the pieces needed for the
         * HubServicesFactory, the wrapper to get/use all the Hub API's
         */
        RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(intLogger)
        HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection)

        /*
         * let's create the project/version in the Hub
         */
        ProjectRequest projectRequest = createProjectRequest()
        ProjectDataService projectDataService = hubServicesFactory.createProjectDataService()
        String projectUrl = projectDataService.createHubProject(projectRequest)

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
        def hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        def projectDataService = hubServicesFactory.createProjectDataService()
        ProjectView createdProject = projectDataService.getProjectByName(PROJECT_NAME)
        projectDataService.deleteHubProject(createdProject)
    }
}
