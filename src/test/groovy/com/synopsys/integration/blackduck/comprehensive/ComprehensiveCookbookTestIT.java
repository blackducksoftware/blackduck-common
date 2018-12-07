package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.SignatureScannerCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatch;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchBuilder;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchManager;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanTarget;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

@Tag("integration")
public class ComprehensiveCookbookTestIT {
    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void createProjectVersion() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        deleteIfProjectExists(logger, projectService, blackDuckService, testProjectName);

        // get the count of all projects now
        final int projectCount = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        // post the project
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        final ProjectView projectItem = projectVersionWrapper.getProjectView();
        final Optional<ProjectView> projectItemFromName = projectService.getProjectByName(testProjectName);

        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size();

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setVersionName("RestConnectionTest");
        final ProjectVersionView projectVersionItem = projectService.createProjectVersion(projectItem, projectVersionRequest);
        final Optional<ProjectVersionView> projectVersionItemFromName = projectService.getProjectVersion(projectItem, "RestConnectionTest");

        // should return the same project version
        assertTrue(projectVersionItemFromName.isPresent());
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.get().toString());

        assertTrue(blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        deleteIfProjectExists(logger, projectService, blackDuckService, testProjectName);

        // get the count of all projects now
        final int projectCount = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        final String versionName = "RestConnectionTest";
        final ProjectVersionDistributionType distribution = ProjectVersionDistributionType.INTERNAL;
        final ProjectVersionPhaseType phase = ProjectVersionPhaseType.DEVELOPMENT;
        final ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder();
        projectBuilder.setProjectName(testProjectName);
        projectBuilder.setVersionName(versionName);
        projectBuilder.setPhase(phase);
        projectBuilder.setDistribution(distribution);

        final ProjectRequest projectRequest = projectBuilder.build();

        // post the project
        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        final ProjectView projectItem = projectVersionWrapper.getProjectView();
        final Optional<ProjectView> projectItemFromName = projectService.getProjectByName(testProjectName);

        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final Optional<ProjectVersionView> projectVersionItem = projectService.getProjectVersion(projectItem, versionName);
        assertTrue(projectVersionItem.isPresent());
        assertEquals(versionName, projectVersionItem.get().getVersionName());

        // should return the same project version
        assertEquals(projectVersionWrapper.getProjectVersionView().toString(), projectVersionItem.get().toString());
    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final String projectName = "ek_mtglist";
        final String projectVersionName = "0.0.1";
        final String codeLocationName = "ek_mtglist Black Duck I/O Export";
        final String policyRuleName = "Test Rule for comprehensive policy status/bdio";
        final String componentName = "Apache POI";
        final String componentVersion = "3.9";
        final String groupId = "org.apache.poi";
        final String artifact = "poi";
        final CheckPolicyData checkPolicyData = new CheckPolicyData(projectName, projectVersionName, codeLocationName, policyRuleName, componentName, componentVersion, groupId, artifact);
        final BlackDuckServices blackDuckServices = new BlackDuckServices();

        setupPolicyCheck(blackDuckServices, checkPolicyData);

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        final UploadRunner uploadRunner = new UploadRunner(blackDuckServices.logger, blackDuckServices.blackDuckService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, file));
        final BdioUploadCodeLocationCreationRequest codeLocationCreationRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        blackDuckServices.codeLocationCreationService.createCodeLocationsAndWait(codeLocationCreationRequest, 15 * 60);

        completePolicyCheck(blackDuckServices, checkPolicyData);
    }

    @Test
    public void testPolicyStatusFromSignatureScan() throws Exception {
        final String projectName = "scan-hub-artifactory-test";
        final String projectVersionName = "1.0.0_test";
        final String codeLocationName = "scan_artifactory_code_location";
        final String policyRuleName = "Test Rule for comprehensive policy status/scan";
        final String componentName = "Apache Ant";
        final String componentVersion = "1.9.7";
        final String groupId = "org.apache.ant";
        final String artifact = "ant";
        final CheckPolicyData checkPolicyData = new CheckPolicyData(projectName, projectVersionName, codeLocationName, policyRuleName, componentName, componentVersion, groupId, artifact);
        final BlackDuckServices blackDuckServices = new BlackDuckServices();

        setupPolicyCheck(blackDuckServices, checkPolicyData);

        final File scanFile = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
        final File parentDirectory = scanFile.getParentFile();
        final File installDirectory = new File(parentDirectory, "scanner_install");
        final File outputDirectory = new File(parentDirectory, "scanner_output");

        // perform the scan
        final ScanBatchManager scanBatchManager = ScanBatchManager.createDefaultScanManager(blackDuckServices.logger, blackDuckServices.blackDuckServerConfig);
        final ScanBatchBuilder scanBatchBuilder = new ScanBatchBuilder();
        scanBatchBuilder.fromBlackDuckServerConfig(blackDuckServices.blackDuckServerConfig);
        scanBatchBuilder.installDirectory(installDirectory);
        scanBatchBuilder.outputDirectory(outputDirectory);
        scanBatchBuilder.projectAndVersionNames(projectName, projectVersionName);
        scanBatchBuilder.addTarget(ScanTarget.createBasicTarget(scanFile.getAbsolutePath(), codeLocationName));
        final ScanBatch scanBatch = scanBatchBuilder.build();
        final SignatureScannerCodeLocationCreationRequest codeLocationCreationRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchManager, scanBatch);

        final ScanBatchOutput scanBatchOutput = blackDuckServices.codeLocationCreationService.createCodeLocationsAndWait(codeLocationCreationRequest, 15 * 60);
        for (final ScanCommandOutput scanCommandOutput : scanBatchOutput.getOutputs()) {
            assertTrue(scanCommandOutput.getResult() == Result.SUCCESS);
            assertNotNull(scanCommandOutput.getDryRunFile());
        }

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
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
            final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

            final List<ProjectView> allProjects = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
            System.out.println(String.format("project count: %d", allProjects.size()));
            for (final ProjectView projectItem : allProjects) {
                final List<ProjectVersionView> allProjectVersions = blackDuckService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE);
                System.out.println(projectItem.toString());
                System.out.println(String.format("version count: %d", allProjectVersions.size()));
                for (final ProjectVersionView projectVersionItem : allProjectVersions) {
                    System.out.println(projectVersionItem.toString());
                }
            }
        }
    }

    private <T extends BlackDuckResponse> void assertGettingAll(final BlackDuckPathMultipleResponses<T> pathResponses, final String labelForOutput) throws IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

        final BlackDuckPageResponse<T> pageResponse = blackDuckService.getPageResponses(pathResponses, true);
        assertTrue(pageResponse.getTotalCount() > 0);
        assertEquals(pageResponse.getTotalCount(), pageResponse.getItems().size());

        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            System.out.println(String.format("%s count: %d", labelForOutput, pageResponse.getTotalCount()));
            for (final BlackDuckResponse blackDuckResponse : pageResponse.getItems()) {
                System.out.println(String.format("%s: %s", labelForOutput, blackDuckResponse.toString()));
            }
        }
    }

    private void deleteIfProjectExists(final IntLogger logger, final ProjectService projectService, final BlackDuckService blackDuckService, final String projectName) throws Exception {
        try {
            final Optional<ProjectView> project = projectService.getProjectByName(projectName);
            if (project.isPresent()) {
                blackDuckService.delete(project.get());
            }
        } catch (final BlackDuckIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

    private void setupPolicyCheck(final BlackDuckServices blackDuckServices, final CheckPolicyData checkPolicyData) throws IntegrationException {
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
        Optional<PolicyRuleViewV2> policyRuleView = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        if (policyRuleView.isPresent()) {
            blackDuckServices.blackDuckService.delete(policyRuleView.get());
        }
        policyRuleView = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        assertFalse(policyRuleView.isPresent());

        // make sure there is a policy that will be in violation
        final ExternalId externalId = new ExternalIdFactory().createMavenExternalId(checkPolicyData.groupId, checkPolicyData.artifact, checkPolicyData.componentVersion);
        blackDuckServices.policyRuleService.createPolicyRuleForExternalId(blackDuckServices.componentService, externalId, checkPolicyData.policyRuleName);
    }

    private void completePolicyCheck(final BlackDuckServices blackDuckServices, final CheckPolicyData checkPolicyData) throws IntegrationException {
        // the project/version should now be created
        final Optional<ProjectVersionWrapper> projectVersionWrapper = blackDuckServices.projectService.getProjectVersion(checkPolicyData.projectName, checkPolicyData.projectVersionName);
        assertTrue(projectVersionWrapper.isPresent());
        final ProjectVersionView projectVersion = projectVersionWrapper.get().getProjectVersionView();
        assertNotNull(projectVersion);

        // check that we have components in the BOM
        final List<VersionBomComponentView> bomComponents = blackDuckServices.blackDuckService.getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        assertTrue(bomComponents.size() > 0);

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if (checkPolicyData.componentName.equals(comp.getComponentName()) && (checkPolicyData.componentVersion.equals(comp.getComponentVersionName()))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals("DYNAMICALLY_LINKED", foundComp.getUsages().get(0).toString());

        // verify the policy
        final ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();
        final Optional<VersionBomPolicyStatusView> policyStatusItem = blackDuckServices.projectService.getPolicyStatusForVersion(projectVersionView);
        assertTrue(policyStatusItem.isPresent());
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, policyStatusItem.get().getOverallStatus());

        final Optional<PolicyRuleViewV2> checkPolicyRule = blackDuckServices.policyRuleService.getPolicyRuleViewByName(checkPolicyData.policyRuleName);
        assertTrue(checkPolicyRule.isPresent());
    }

    private class BlackDuckServices {
        public IntLogger logger;
        public BlackDuckServicesFactory blackDuckServicesFactory;
        public BlackDuckServerConfig blackDuckServerConfig;
        public ProjectService projectService;
        public CodeLocationService codeLocationService;
        public BlackDuckService blackDuckService;
        public ComponentService componentService;
        public PolicyRuleService policyRuleService;
        public CodeLocationCreationService codeLocationCreationService;

        public BlackDuckServices() throws IntegrationException {
            logger = new PrintStreamIntLogger(System.out, LogLevel.OFF);
            blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory(logger);
            blackDuckServerConfig = restConnectionTestHelper.getBlackDuckServerConfig();
            projectService = blackDuckServicesFactory.createProjectService();
            codeLocationService = blackDuckServicesFactory.createCodeLocationService();
            blackDuckService = blackDuckServicesFactory.createBlackDuckService();
            componentService = blackDuckServicesFactory.createComponentService();
            policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
            codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        }
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

        public CheckPolicyData(final String projectName, final String projectVersionName, final String codeLocationName, final String policyRuleName, final String componentName, final String componentVersion, final String groupId,
                final String artifact) {
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
