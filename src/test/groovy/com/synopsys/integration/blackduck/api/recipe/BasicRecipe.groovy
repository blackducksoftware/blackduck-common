package com.synopsys.integration.blackduck.api.recipe

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchRunner
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper
import com.synopsys.integration.blackduck.service.*
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.log.BufferedIntLogger
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.util.IntEnvironmentVariables
import org.junit.jupiter.api.BeforeEach

class BasicRecipe {
    public static final String PROJECT_NAME = 'My Recipe Project'
    public static final String PROJECT_VERSION_NAME = '0.0.1-SNAPSHOT'
    public static final IntHttpClientTestHelper restConnectionTestHelper = new IntHttpClientTestHelper()

    protected Gson gson
    protected ObjectMapper objectMapper

    protected IntLogger logger
    protected BlackDuckServicesFactory blackDuckServicesFactory
    protected BlackDuckService blackDuckService
    protected BlackDuckBucketService blackDuckBucketService
    protected ProjectService projectService
    protected ProjectUsersService projectUsersService
    protected ProjectBomService projectBomService
    protected CodeLocationService codeLocationService
    protected NotificationService notificationService
    protected CodeLocationCreationService codeLocationCreationService
    protected PolicyRuleService policyRuleService
    protected UploadBatchRunner uploadRunner
    protected MediaTypeDiscovery mediaTypeDiscovery

    @BeforeEach
    void startRecipe() {
        /*
         * the integration logger used to display log messages from our code
         * within a 3rd party integration environment
         */
        logger = new BufferedIntLogger()

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
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createCredentialsBlackDuckHttpClient(logger)
        IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        gson = BlackDuckServicesFactory.createDefaultGson()
        objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper()
        mediaTypeDiscovery = BlackDuckServicesFactory.createDefaultMediaTypeDiscovery()

        blackDuckServicesFactory = new BlackDuckServicesFactory(intEnvironmentVariables, gson, objectMapper, blackDuckHttpClient, logger, mediaTypeDiscovery)
        blackDuckService = blackDuckServicesFactory.getBlackDuckService()
        blackDuckBucketService = blackDuckServicesFactory.createBlackDuckBucketService()
        projectService = blackDuckServicesFactory.createProjectService()
        projectUsersService = blackDuckServicesFactory.createProjectUsersService()
        projectBomService = blackDuckServicesFactory.createProjectBomService()
        codeLocationService = blackDuckServicesFactory.createCodeLocationService()
        notificationService = blackDuckServicesFactory.createNotificationService()
        codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService()
        policyRuleService = blackDuckServicesFactory.createPolicyRuleService()

        uploadRunner = new UploadBatchRunner(logger, blackDuckService)
    }

    ProjectSyncModel createProjectSyncModel(String projectName, String projectVersionName) {
        /*
         * the ProjectSyncModel is a wrapper around creating a
         * ProjectRequest that will also include a ProjectVersionRequest to
         * create both a project in Black Duck and a version for that created
         * project - a project must have at least one version
         */
        ProjectSyncModel projectSyncModel = new ProjectSyncModel(projectName, projectVersionName)
        projectSyncModel.description = 'A sample testing project to demonstrate blackduck-common capabilities.'
        projectSyncModel.phase = ProjectVersionPhaseType.DEVELOPMENT
        projectSyncModel.distribution = LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.OPENSOURCE

        projectSyncModel
    }

    void deleteProject(String projectName) {
        if (null != projectName) {
            Optional<ProjectView> project = projectService.getProjectByName(projectName)
            if (project.isPresent()) {
                deleteProject(project.get())
            }
        }
    }

    void deleteProject(ProjectView projectView) {
        if (null != projectView) {
            blackDuckService.delete(projectView)
        }
    }

    void deleteCodeLocation(String codeLocationName) {
        Optional<CodeLocationView> codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName)
        if (codeLocationView.isPresent()) {
            blackDuckService.delete(codeLocationView.get())
        }
    }

}
