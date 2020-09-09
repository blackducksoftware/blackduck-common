package com.synopsys.integration.blackduck.comprehensive.recipe;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchRunner;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.blackduck.service.dataservice.NotificationService;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class BasicRecipe {
    public static final String PROJECT_NAME = "My Recipe Project";
    public static final String PROJECT_VERSION_NAME = "0.0.1-SNAPSHOT";
    public static final IntHttpClientTestHelper restConnectionTestHelper = new IntHttpClientTestHelper();

    protected Gson gson;
    protected ObjectMapper objectMapper;
    protected ExecutorService executorService;
    protected IntLogger logger;
    protected BlackDuckServicesFactory blackDuckServicesFactory;
    protected BlackDuckService blackDuckService;
    protected BlackDuckBucketService blackDuckBucketService;
    protected ProjectService projectService;
    protected ProjectUsersService projectUsersService;
    protected ProjectBomService projectBomService;
    protected CodeLocationService codeLocationService;
    protected NotificationService notificationService;
    protected CodeLocationCreationService codeLocationCreationService;
    protected PolicyRuleService policyRuleService;
    protected UploadBatchRunner uploadRunner;
    protected RequestFactory requestFactory;

    @BeforeEach
    public void startRecipe() {
        /*
         * the integration logger used to display log messages from our code
         * within a 3rd party integration environment
         */
        logger = new BufferedIntLogger();

        /*
         * any usage of Black Duck API's has to begin with a url for the Black Duck
         * server and either:
         *   an API key
         * --or--
         *   a username and a password (this is what we're using here)
         */
        BlackDuckServerConfig blackDuckServerConfig = restConnectionTestHelper.getBlackDuckServerConfig();

        /*
         * next, we need to create the pieces needed for the
         * BlackDuckServicesFactory, the wrapper to get/use all Black Duck API's
         */
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createCredentialsBlackDuckHttpClient(logger);
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();
        gson = BlackDuckServicesFactory.createDefaultGson();
        objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        executorService = BlackDuckServicesFactory.NO_THREAD_EXECUTOR_SERVICE;
        requestFactory = BlackDuckServicesFactory.createDefaultRequestFactory();

        blackDuckServicesFactory = new BlackDuckServicesFactory(intEnvironmentVariables, gson, objectMapper, executorService, blackDuckHttpClient, logger, requestFactory);
        blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        blackDuckBucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        projectService = blackDuckServicesFactory.createProjectService();
        projectUsersService = blackDuckServicesFactory.createProjectUsersService();
        projectBomService = blackDuckServicesFactory.createProjectBomService();
        codeLocationService = blackDuckServicesFactory.createCodeLocationService();
        notificationService = blackDuckServicesFactory.createNotificationService();
        codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        policyRuleService = blackDuckServicesFactory.createPolicyRuleService();

        uploadRunner = new UploadBatchRunner(logger, blackDuckService, requestFactory, executorService);
    }

    public ProjectSyncModel createProjectSyncModel(String projectName, String projectVersionName) {
        /*
         * the ProjectSyncModel is a wrapper around creating a
         * ProjectRequest that will also include a ProjectVersionRequest to
         * create both a project in Black Duck and a version for that created
         * project - a project must have at least one version
         */
        ProjectSyncModel projectSyncModel = new ProjectSyncModel(projectName, projectVersionName);
        projectSyncModel.setDescription("A sample testing project to demonstrate blackduck-common capabilities.");
        projectSyncModel.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectSyncModel.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.OPENSOURCE);

        return projectSyncModel;
    }

    public void deleteProject(String projectName) throws IntegrationException {
        if (null != projectName) {
            Optional<ProjectView> project = projectService.getProjectByName(projectName);
            if (project.isPresent()) {
                deleteProject(project.get());
            }
        }
    }

    public void deleteProject(ProjectView projectView) throws IntegrationException {
        if (null != projectView) {
            blackDuckService.delete(projectView);
        }
    }

    public void deleteCodeLocation(String codeLocationName) throws IntegrationException {
        Optional<CodeLocationView> codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        if (codeLocationView.isPresent()) {
            blackDuckService.delete(codeLocationView.get());
        }
    }

}
