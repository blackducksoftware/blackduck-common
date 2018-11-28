package com.synopsys.integration.blackduck.api.recipe

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory
import com.synopsys.integration.blackduck.service.CodeLocationService
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder
import com.synopsys.integration.log.BufferedIntLogger
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.rest.connection.RestConnection
import org.junit.jupiter.api.BeforeEach

class BasicRecipe {
    public static final String PROJECT_NAME = 'My Recipe Project'
    public static final String PROJECT_VERSION_NAME = '0.0.1-SNAPSHOT'
    public static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    public BlackDuckServicesFactory blackDuckServicesFactory

    protected Gson gson;
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void startRecipe() {
        /*
         * the integration logger used to display log messages from our code
         * within a 3rd party integration environment
         */
        IntLogger intLogger = new BufferedIntLogger()

        /*
         * any usage of Black Duck API's has to begin with a url for the Black Duck
         * server and either:
         *   an API key
         * --or--
         *   a username and a password (this is what we're using here)
         */
        BlackDuckServerConfig blackDuckServerConfig = restConnectionTestHelper.getBlackDuckServerConfig()

        /*
         * next, we need to create the pieces needed for the
         * BlackDuckServicesFactory, the wrapper to get/use all Black Duck API's
         */
        RestConnection restConnection = blackDuckServerConfig.createCredentialsRestConnection(intLogger)
        gson = BlackDuckServicesFactory.createDefaultGson()
        objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper()

        blackDuckServicesFactory = new BlackDuckServicesFactory(gson, objectMapper, restConnection, intLogger)
    }

    public ProjectRequest createProjectRequest(String projectName, String projectVersionName) {
        /*
         * the ProjectRequestBuilder is a simple wrapper around creating a
         * ProjectRequest that will also include a ProjectVersionRequest to
         * create both a project in Black Duck and a version for that created
         * project - a project must have at least one version
         */
        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder()
        projectRequestBuilder.projectName = projectName
        projectRequestBuilder.description = 'A sample testing project to demonstrate blackduck-common capabilities.'
        projectRequestBuilder.versionName = projectVersionName
        projectRequestBuilder.phase = ProjectVersionPhaseType.DEVELOPMENT.name()
        projectRequestBuilder.distribution = ProjectVersionDistributionType.OPENSOURCE.name()

        projectRequestBuilder.build()
    }

    public void deleteProject(String projectName) {
        ProjectService projectDataService = blackDuckServicesFactory.createProjectService()
        Optional<ProjectView> project = projectDataService.getProjectByName(projectName)
        if (project.isPresent()) {
            projectDataService.deleteProject(project.get())
        }
    }

    public void deleteCodeLocation(String codeLocationName) {
        CodeLocationService codeLocationService = blackDuckServicesFactory.createCodeLocationService()
        CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName)
        codeLocationService.deleteCodeLocation(codeLocationView)
    }
}
