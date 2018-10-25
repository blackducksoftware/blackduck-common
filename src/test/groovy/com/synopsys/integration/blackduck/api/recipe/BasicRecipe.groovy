package com.synopsys.integration.blackduck.api.recipe

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.configuration.HubServerConfig
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.CodeLocationService
import com.synopsys.integration.blackduck.service.HubServicesFactory
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.rest.connection.RestConnection
import com.synopsys.integration.test.tool.TestLogger
import org.junit.Before

class BasicRecipe {
    public static final String PROJECT_NAME = 'My Recipe Project'
    public static final String PROJECT_VERSION_NAME = '0.0.1-SNAPSHOT'
    public static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    public HubServicesFactory hubServicesFactory

    protected Gson gson;
    protected JsonParser jsonParser;

    @Before
    public void startRecipe() {
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
        gson = HubServicesFactory.createDefaultGson()
        jsonParser = HubServicesFactory.createDefaultJsonParser()
        hubServicesFactory = new HubServicesFactory(gson, jsonParser, restConnection, intLogger)
    }

    public ProjectRequest createProjectRequest(String projectName, String projectVersionName) {
        /*
         * the ProjectRequestBuilder is a simple wrapper around creating a
         * ProjectRequest that will also include a ProjectVersionRequest to
         * create both a project in the Hub and a version for that created
         * project - a project must have at least one version
         */
        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.projectName = projectName
        projectRequestBuilder.description = 'A sample testing project to demonstrate hub-common capabilities.'
        projectRequestBuilder.versionName = projectVersionName
        projectRequestBuilder.phase = ProjectVersionPhaseType.DEVELOPMENT.name()
        projectRequestBuilder.distribution = ProjectVersionDistributionType.OPENSOURCE.name()

        projectRequestBuilder.build()
    }

    public void deleteProject(String projectName) {
        ProjectService projectDataService = hubServicesFactory.createProjectService()
        Optional<ProjectView> project = projectDataService.getProjectByName(projectName)
        if (project.isPresent()) {
            projectDataService.deleteProject(project.get())
        }
    }

    public void deleteCodeLocation(String codeLocationName) {
        CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService()
        CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName)
        codeLocationService.deleteCodeLocation(codeLocationView)
    }
}
