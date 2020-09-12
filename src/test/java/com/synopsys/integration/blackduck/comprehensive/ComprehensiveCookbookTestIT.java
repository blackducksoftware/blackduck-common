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
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.SignatureScannerService;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.PagedRequest;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.http.transform.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComprehensiveCookbookTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(new Gson(), new ObjectMapper(), new SilentIntLogger());

    @Test
    public void createProjectVersion() throws Exception {
        String testProjectName = intHttpClientTestHelper.getProperty("TEST_CREATE_PROJECT");

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckService, testProjectName);

        // get the count of all projects now
        int projectCount = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        // create the project
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        ProjectView projectItem = projectVersionWrapper.getProjectView();
        Optional<ProjectView> projectItemFromName = projectService.getProjectByName(testProjectName);

        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        int projectCountAfterCreate = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        int projectVersionCount = blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size();

        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setVersionName("RestConnectionTest");
        ProjectVersionView projectVersionItem = projectService.createProjectVersion(projectItem, projectVersionRequest);
        Optional<ProjectVersionView> projectVersionItemFromName = projectService.getProjectVersion(projectItem, "RestConnectionTest");

        // should return the same project version
        assertTrue(projectVersionItemFromName.isPresent());
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.get().toString());

        assertTrue(blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        String testProjectName = intHttpClientTestHelper.getProperty("TEST_CREATE_PROJECT");

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckService, testProjectName);

        // get the count of all projects now
        int projectCount = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        String versionName = "RestConnectionTest";
        LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType distribution = LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL;
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

        int projectCountAfterCreate = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
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

        UserView currentUser = blackDuckServices.blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
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

        UserView currentUser = blackDuckServices.blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        Date userStartDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        Date systemStartDate = blackDuckServices.notificationService.getLatestNotificationDate();

        File scanFile = intHttpClientTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
        File parentDirectory = scanFile.getParentFile();
        File installDirectory = new File(parentDirectory, "scanner_install");
        File outputDirectory = new File(parentDirectory, "scanner_output");

        // perform the scan
        ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.fromBlackDuckServerConfig(blackDuckServices.blackDuckServerConfig);
        scanBatchBuilder.installDirectory(installDirectory);
        scanBatchBuilder.outputDirectory(outputDirectory);
        scanBatchBuilder.projectAndVersionNames(projectName, projectVersionName);
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(scanFile.getAbsolutePath(), codeLocationName));
        ScanBatch scanBatch = scanBatchBuilder.build();

        SignatureScannerService signatureScannerService = blackDuckServices.blackDuckServicesFactory.createSignatureScannerService();
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
        assertGettingAll(ApiDiscovery.PROJECTS_LINK_RESPONSE, "project");
    }

    @Test
    public void testGettingAllCodeLocations() throws IntegrationException {
        assertGettingAll(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, "code location");
    }

    @Test
    public void testGettingAllUsers() throws IntegrationException {
        assertGettingAll(ApiDiscovery.USERS_LINK_RESPONSE, "user");
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        if (Boolean.parseBoolean(intHttpClientTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();

            List<ProjectView> allProjects = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
            System.out.println(String.format("project count: %d", allProjects.size()));
            for (ProjectView projectItem : allProjects) {
                List<ProjectVersionView> allProjectVersions = blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE);
                System.out.println(projectItem.toString());
                System.out.println(String.format("version count: %d", allProjectVersions.size()));
                for (ProjectVersionView projectVersionItem : allProjectVersions) {
                    System.out.println(projectVersionItem.toString());
                }
            }
        }
    }

    private <T extends BlackDuckResponse> void assertGettingAll(BlackDuckPathMultipleResponses<T> pathResponses, String labelForOutput) throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckResponsesTransformer blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckServicesFactory.getBlackDuckHttpClient(), blackDuckJsonTransformer);

        HttpUrl baseUrl = blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl();
        HttpUrl getUrl = baseUrl.appendRelativeUrl(pathResponses.getBlackDuckPath().getPath());
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new Request.Builder());
        requestBuilder.url(getUrl);
        PagedRequest pagedRequest = new PagedRequest(requestBuilder);

        BlackDuckPageResponse<T> pageResponse = blackDuckResponsesTransformer.getAllResponses(pagedRequest, pathResponses.getResponseClass());
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
        List<ProjectVersionComponentView> bomComponents = blackDuckServices.blackDuckService.getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
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
        assertEquals(PolicyStatusType.IN_VIOLATION, policyStatusItem.get().getOverallStatus());

        Optional<PolicyRuleView> checkPolicyRule = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        assertTrue(checkPolicyRule.isPresent());

        deletePolicyData(blackDuckServices, checkPolicyData);
    }

    private void deletePolicyData(BlackDuckServices blackDuckServices, CheckPolicyData checkPolicyData) throws IntegrationException {
        // delete the project, if it exists
        Optional<ProjectView> projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(checkPolicyData.projectName);
        if (projectThatShouldNotExist.isPresent()) {
            blackDuckServices.blackDuckService.delete(projectThatShouldNotExist.get());
        }
        projectThatShouldNotExist = blackDuckServices.projectService.getProjectByName(checkPolicyData.projectName);
        assertFalse(projectThatShouldNotExist.isPresent());

        // delete the code location if it exists
        Optional<CodeLocationView> codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(checkPolicyData.codeLocationName);
        if (codeLocationView.isPresent()) {
            blackDuckServices.blackDuckService.delete(codeLocationView.get());
        }
        codeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(checkPolicyData.codeLocationName);
        assertFalse(codeLocationView.isPresent());

        // delete the policy rule if it exists
        Optional<PolicyRuleView> policyRuleView = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        if (policyRuleView.isPresent()) {
            blackDuckServices.blackDuckService.delete(policyRuleView.get());
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
