package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComprehensiveCookbookTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory(new SilentIntLogger());
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory = blackDuckServicesFactory.getBlackDuckRequestBuilderFactory();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer = blackDuckServicesFactory.getBlackDuckResponsesTransformer();

    public ComprehensiveCookbookTestIT() throws IntegrationException {}

    @Test
    public void createProjectVersion() throws Exception {
        String testProjectName = intHttpClientTestHelper.getProperty("TEST_CREATE_PROJECT");

        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, testProjectName);

        // get the count of all projects now
        int projectCount = blackDuckApiClient.getAllResponses(apiDiscovery.metaProjectsLink()).size();

        // create the project
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        ProjectView projectItem = projectVersionWrapper.getProjectView();
        Optional<ProjectView> projectItemFromName = projectService.getProjectByName(testProjectName);

        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        int projectCountAfterCreate = blackDuckApiClient.getAllResponses(apiDiscovery.metaProjectsLink()).size();
        assertTrue(projectCountAfterCreate > projectCount);

        int projectVersionCount = blackDuckApiClient.getAllResponses(projectItem.metaVersionsLink()).size();

        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setVersionName("RestConnectionTest");
        ProjectVersionView projectVersionItem = projectService.createProjectVersion(projectItem, projectVersionRequest);
        Optional<ProjectVersionView> projectVersionItemFromName = projectService.getProjectVersion(projectItem, "RestConnectionTest");

        // should return the same project version
        assertTrue(projectVersionItemFromName.isPresent());
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.get().toString());

        assertTrue(blackDuckApiClient.getAllResponses(projectItem.metaVersionsLink()).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        String testProjectName = intHttpClientTestHelper.getProperty("TEST_CREATE_PROJECT");

        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckApiClient, testProjectName);

        // get the count of all projects now
        int projectCount = blackDuckApiClient.getAllResponses(apiDiscovery.metaProjectsLink()).size();

        String versionName = "RestConnectionTest";
        ProjectVersionDistributionType distribution = ProjectVersionDistributionType.INTERNAL;
        ProjectVersionPhaseType phase = ProjectVersionPhaseType.DEVELOPMENT;
        ProjectSyncModel projectSyncModel = new ProjectSyncModel(testProjectName, versionName);
        projectSyncModel.setPhase(phase);
        projectSyncModel.setDistribution(distribution);

        ProjectRequest projectRequest = projectSyncModel.createProjectRequest();

        // create the project
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        ProjectView projectItem = projectVersionWrapper.getProjectView();
        Optional<ProjectView> projectItemFromName = projectService.getProjectByName(testProjectName);

        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        int projectCountAfterCreate = blackDuckApiClient.getAllResponses(apiDiscovery.metaProjectsLink()).size();
        assertTrue(projectCountAfterCreate > projectCount);

        Optional<ProjectVersionView> projectVersionItem = projectService.getProjectVersion(projectItem, versionName);
        assertTrue(projectVersionItem.isPresent());
        assertEquals(versionName, projectVersionItem.get().getVersionName());

        // should return the same project version
        assertEquals(projectVersionWrapper.getProjectVersionView().toString(), projectVersionItem.get().toString());
    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        String projectName = "ek_mtglist";
        String projectVersionName = "0.0.1";
        String codeLocationName = "ek_mtglist Black Duck I/O Export";
        String policyRuleName = "Test Rule for comprehensive policy status/bdio";
        String componentName = "Apache POI";
        String componentVersion = "3.9";
        String groupId = "org.apache.poi";
        String artifact = "poi";
        CheckPolicyData checkPolicyData = new CheckPolicyData(projectName, projectVersionName, codeLocationName, policyRuleName, componentName, componentVersion, groupId, artifact);
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);

        setupPolicyCheck(blackDuckServices, checkPolicyData);

        UserView currentUser = blackDuckServices.userService.findCurrentUser();
        Date userStartDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        Date systemStartDate = blackDuckServices.notificationService.getLatestNotificationDate();

        // import the bdio
        File file = intHttpClientTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        UploadBatch uploadBatch = new UploadBatch(UploadTarget.createDefault(new NameVersion(projectName, projectVersionName), codeLocationName, file));

        BdioUploadService bdioUploadService = blackDuckServices.blackDuckServicesFactory.createBdioUploadService();
        UploadBatchOutput uploadBatchOutput = bdioUploadService.uploadBdioAndWait(uploadBatch, 15 * 60);
        for (UploadOutput uploadOutput : uploadBatchOutput) {
            assertEquals(Result.SUCCESS, uploadOutput.getResult());
        }

        VerifyNotifications.verify(currentUser, blackDuckServices.notificationService, userStartDate, systemStartDate);

        completePolicyCheck(blackDuckServices, checkPolicyData);
    }

    @Test
    public void testPolicyStatusFromSignatureScan() throws Exception {
        String projectName = "scan-hub-artifactory-test";
        String projectVersionName = "1.0.0_test";
        String codeLocationName = "scan_artifactory_code_location";
        String policyRuleName = "Test Rule for comprehensive policy status/scan";
        String componentName = "Apache Ant";
        String componentVersion = "1.9.7";
        String groupId = "org.apache.ant";
        String artifact = "ant";
        CheckPolicyData checkPolicyData = new CheckPolicyData(projectName, projectVersionName, codeLocationName, policyRuleName, componentName, componentVersion, groupId, artifact);
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);

        setupPolicyCheck(blackDuckServices, checkPolicyData);

        UserView currentUser = blackDuckServices.userService.findCurrentUser();
        Date userStartDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        Date systemStartDate = blackDuckServices.notificationService.getLatestNotificationDate();

        File scanFile = intHttpClientTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
        File parentDirectory = scanFile.getParentFile();
        File installDirectory = new File(parentDirectory, "scanner_install");
        File outputDirectory = new File(parentDirectory, "scanner_output");

        // perform the scan
        ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.fromBlackDuckServerConfig(blackDuckServices.blackDuckServerConfig);
        scanBatchBuilder.outputDirectory(outputDirectory);
        scanBatchBuilder.projectAndVersionNames(projectName, projectVersionName);
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(scanFile.getAbsolutePath(), codeLocationName));
        ScanBatch scanBatch = scanBatchBuilder.build();

        SignatureScannerService signatureScannerService = blackDuckServices.blackDuckServicesFactory.createSignatureScannerService(installDirectory);
        ScanBatchOutput scanBatchOutput = signatureScannerService.performSignatureScanAndWait(scanBatch, 15 * 60);

        for (ScanCommandOutput scanCommandOutput : scanBatchOutput) {
            if (!Result.SUCCESS.equals(scanCommandOutput.getResult())) {
                scanCommandOutput.getException().ifPresent(exception -> System.out.println(String.format("Scan exception: %s", exception.getMessage())));
                scanCommandOutput.getErrorMessage().ifPresent(msg -> System.out.println(String.format("Scan error message: %s", msg)));
                scanCommandOutput.getScanExitCode().ifPresent(exitCode -> System.out.println(String.format("Scan exit code: %s", exitCode)));
                System.out.println("Scan command start");
                System.out.println(scanCommandOutput.getExecutedScanCommand());
                System.out.println("Scan command end");
            }
            assertEquals(Result.SUCCESS, scanCommandOutput.getResult());
            assertNotNull(scanCommandOutput.getDryRunFile());
        }

        VerifyNotifications.verify(currentUser, blackDuckServices.notificationService, userStartDate, systemStartDate);

        completePolicyCheck(blackDuckServices, checkPolicyData);
    }

    @Test
    public void testGettingAllProjects() throws IntegrationException {
        assertGettingAll(apiDiscovery.metaProjectsLink(), "project");
    }

    @Test
    public void testGettingAllCodeLocations() throws IntegrationException {
        assertGettingAll(apiDiscovery.metaCodelocationsLink(), "code location");
    }

    @Test
    public void testGettingAllUsers() throws IntegrationException {
        assertGettingAll(apiDiscovery.metaUsersLink(), "user");
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        if (Boolean.parseBoolean(intHttpClientTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

            List<ProjectView> allProjects = blackDuckApiClient.getAllResponses(apiDiscovery.metaProjectsLink());
            System.out.println(String.format("project count: %d", allProjects.size()));
            for (ProjectView projectItem : allProjects) {
                List<ProjectVersionView> allProjectVersions = blackDuckApiClient.getAllResponses(projectItem.metaVersionsLink());
                System.out.println(projectItem.toString());
                System.out.println(String.format("version count: %d", allProjectVersions.size()));
                for (ProjectVersionView projectVersionItem : allProjectVersions) {
                    System.out.println(projectVersionItem.toString());
                }
            }
        }
    }

    private <T extends BlackDuckResponse> void assertGettingAll(UrlMultipleResponses<T> urlResponses, String labelForOutput) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createBlackDuckRequestBuilder();
        BlackDuckRequest<T> blackDuckRequest = new BlackDuckRequest<>(blackDuckRequestBuilder, urlResponses);

        BlackDuckPageResponse<T> pageResponse = blackDuckResponsesTransformer.getAllResponses(blackDuckRequest);
        if (pageResponse.getTotalCount() > 0) {
            assertEquals(pageResponse.getTotalCount(), pageResponse.getItems().size());

            if (Boolean.parseBoolean(intHttpClientTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
                System.out.println(String.format("%s count: %d", labelForOutput, pageResponse.getTotalCount()));
                for (BlackDuckResponse blackDuckResponse : pageResponse.getItems()) {
                    System.out.println(String.format("%s: %s", labelForOutput, blackDuckResponse.toString()));
                }
            }
        }
    }

    private void setupPolicyCheck(BlackDuckServices blackDuckServices, CheckPolicyData checkPolicyData) throws IntegrationException {
        deletePolicyData(blackDuckServices, checkPolicyData);

        // make sure there is a policy that will be in violation
        ExternalId externalId = new ExternalIdFactory().createMavenExternalId(checkPolicyData.groupId, checkPolicyData.artifact, checkPolicyData.componentVersion);
        blackDuckServices.policyRuleService.createPolicyRuleForExternalId(blackDuckServices.componentService, externalId, checkPolicyData.policyRuleName);
    }

    private void completePolicyCheck(BlackDuckServices blackDuckServices, CheckPolicyData checkPolicyData) throws IntegrationException {
        // the project/version should now be created
        Optional<ProjectVersionWrapper> projectVersionWrapper = blackDuckServices.projectService.getProjectVersion(checkPolicyData.projectName, checkPolicyData.projectVersionName);
        assertTrue(projectVersionWrapper.isPresent());
        ProjectVersionView projectVersion = projectVersionWrapper.get().getProjectVersionView();
        assertNotNull(projectVersion);

        // check that we have components in the BOM
        List<ProjectVersionComponentView> bomComponents = blackDuckServices.blackDuckApiClient.getAllResponses(projectVersion.metaComponentsLink());
        assertTrue(bomComponents.size() > 0);

        // Look for testComponent in BOM
        ProjectVersionComponentView foundComp = null;
        for (ProjectVersionComponentView comp : bomComponents) {
            if (checkPolicyData.componentName.equals(comp.getComponentName()) && (checkPolicyData.componentVersion.equals(comp.getComponentVersionName()))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals("DYNAMICALLY_LINKED", foundComp.getUsages().get(0).toString());

        // verify the policy
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();
        Optional<ProjectVersionPolicyStatusView> policyStatusItem = blackDuckServices.projectBomService.getPolicyStatusForVersion(projectVersionView);
        assertTrue(policyStatusItem.isPresent());
        assertEquals(ProjectVersionComponentPolicyStatusType.IN_VIOLATION, policyStatusItem.get().getOverallStatus());

        Optional<PolicyRuleView> checkPolicyRule = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        assertTrue(checkPolicyRule.isPresent());

        deletePolicyData(blackDuckServices, checkPolicyData);
    }

    private void deletePolicyData(BlackDuckServices blackDuckServices, CheckPolicyData checkPolicyData) throws IntegrationException {
        // delete the project, if it exists
        Optional<ProjectView> projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(checkPolicyData.projectName);
        if (projectThatShouldNotExist.isPresent()) {
            blackDuckServices.blackDuckApiClient.delete(projectThatShouldNotExist.get());
        }
        projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(checkPolicyData.projectName);
        assertFalse(projectThatShouldNotExist.isPresent());

        // delete the code location if it exists
        Optional<CodeLocationView> codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(checkPolicyData.codeLocationName);
        if (codeLocationView.isPresent()) {
            blackDuckServices.blackDuckApiClient.delete(codeLocationView.get());
        }
        codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(checkPolicyData.codeLocationName);
        assertFalse(codeLocationView.isPresent());

        // delete the policy rule if it exists
        Optional<PolicyRuleView> policyRuleView = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        if (policyRuleView.isPresent()) {
            blackDuckServices.blackDuckApiClient.delete(policyRuleView.get());
        }
        policyRuleView = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        assertFalse(policyRuleView.isPresent());
    }

    private class CheckPolicyData {
        public String projectName;
        public String projectVersionName;
        public String codeLocationName;
        public String policyRuleName;
        public String componentName;
        public String componentVersion;
        public String groupId;
        public String artifact;

        public CheckPolicyData(String projectName, String projectVersionName, String codeLocationName, String policyRuleName, String componentName, String componentVersion, String groupId,
            String artifact) {
            this.projectName = projectName;
            this.projectVersionName = projectVersionName;
            this.codeLocationName = codeLocationName;
            this.policyRuleName = policyRuleName;
            this.componentName = componentName;
            this.componentVersion = componentVersion;
            this.groupId = groupId;
            this.artifact = artifact;
        }
    }

}
