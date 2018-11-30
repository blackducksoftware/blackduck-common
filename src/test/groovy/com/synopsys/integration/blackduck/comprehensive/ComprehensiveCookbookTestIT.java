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
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

@Tag("integration")
public class ComprehensiveCookbookTestIT {
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private static final long TWENTY_MINUTES = FIVE_MINUTES * 4;

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

        // create the project
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

        // create the project
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
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ComponentService componentService = blackDuckServicesFactory.createComponentService();
        final PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        final CodeLocationCreationService codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        final IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        deleteIfProjectExists(logger, projectService, blackDuckService, "ek_mtglist");

        // make sure there is a policy that will be in violation
        final ExternalId externalId = new ExternalIdFactory().createMavenExternalId("org.apache.poi", "poi", "3.9");
        final String policyNameToDeleteLater = "Test Rule for comprehensive policy status/bdio " + System.currentTimeMillis();
        final String policyRuleUrl = policyRuleService.createPolicyRuleForExternalId(componentService, externalId, policyNameToDeleteLater);

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        final UploadRunner uploadRunner = new UploadRunner(logger, blackDuckService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createWithMediaType("ek_mtglist Black Duck I/O Export", file, "application/ld+json"));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        // make sure we have some code locations now
        final List<CodeLocationView> codeLocationItems = blackDuckService.getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : codeLocationItems) {
                System.out.println("codeLocation: " + codeLocationItem.toString());
            }
        }
        System.out.println("Number of code locations: " + codeLocationItems.size());

        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView item : codeLocationItems) {
                System.out.println("codeLocation: " + item.toString());
            }
        }

        // verify the policy
        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion("ek_mtglist", "0.0.1");
        assertTrue(projectVersionWrapper.isPresent());
        assertNotNull(projectVersionWrapper.get().getProjectVersionView());

        final Optional<VersionBomPolicyStatusView> policyStatusItem = projectService.getPolicyStatusForVersion(projectVersionWrapper.get().getProjectVersionView());
        assertTrue(policyStatusItem.isPresent());
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, policyStatusItem.get().getOverallStatus());
        System.out.println(policyStatusItem.get());

        final Optional<PolicyRuleViewV2> checkPolicyRule = policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
        assertTrue(checkPolicyRule.isPresent());

        blackDuckService.delete(policyRuleUrl);

        final Optional<PolicyRuleViewV2> deleted = policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
        assertFalse(deleted.isPresent());
    }

    //    @Test
    //    public void testMutlipleTargetScanInParallel() throws Exception {
    //        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
    //        final IntLogger logger = blackDuckServicesFactory.getLogger();
    //        logger.setLogLevel(LogLevel.INFO);
    //        final ExecutorService executorService = Executors.newFixedThreadPool(2);
    //        try {
    //            final SignatureScannerService signatureScannerService = blackDuckServicesFactory.createSignatureScannerService(executorService);
    //
    //            final BlackDuckServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();
    //
    //            // codelocation the file in its parent directory
    //            final File scanTarget = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
    //            final File workingDirectory = scanTarget.getParentFile();
    //
    //            final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
    //            hubScanConfigBuilder.setScanMemory(4096);
    //            hubScanConfigBuilder.setDryRun(true);
    //            // download the cli to where ever the file is just for convenience
    //            hubScanConfigBuilder.setToolsDir(workingDirectory);
    //            hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
    //            // always use the canonical path since we validate the paths by string matching
    //            hubScanConfigBuilder.addScanTargetPath(scanTarget.getCanonicalPath());
    //            hubScanConfigBuilder.addScanTargetPath(scanTarget.getParentFile().getCanonicalPath());
    //
    //            hubScanConfigBuilder.setCleanupLogsOnSuccess(true);
    //
    //            final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();
    //
    //            final ScanServiceOutput scanServiceOutput = signatureScannerService.executeScans(hubServerConfig, hubScanConfig, null);
    //            assertNotNull(scanServiceOutput);
    //            assertTrue(scanServiceOutput.getScanCommandOutputs().size() == 2);
    //
    //            for (final ScanCommandOutput scanCommandOutput : scanServiceOutput.getScanCommandOutputs()) {
    //                assertTrue(scanCommandOutput.getResult() == Result.SUCCESS);
    //                assertNotNull(scanCommandOutput.getDryRunFile());
    //            }
    //        } finally {
    //            executorService.shutdownNow();
    //        }
    //    }
    //
    //    @Test
    //    public void testMutlipleTargetScan() throws Exception {
    //        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
    //        final IntLogger logger = blackDuckServicesFactory.getLogger();
    //        logger.setLogLevel(LogLevel.INFO);
    //        final SignatureScannerService signatureScannerService = blackDuckServicesFactory.createSignatureScannerService();
    //
    //        final BlackDuckServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();
    //
    //        // codelocation the file in its parent directory
    //        final File scanTarget = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
    //        final File workingDirectory = scanTarget.getParentFile();
    //
    //        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
    //        hubScanConfigBuilder.setScanMemory(4096);
    //        hubScanConfigBuilder.setDryRun(true);
    //        // download the cli to where ever the file is just for convenience
    //        hubScanConfigBuilder.setToolsDir(workingDirectory);
    //        hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
    //        // always use the canonical path since we validate the paths by string matching
    //        hubScanConfigBuilder.addScanTargetPath(scanTarget.getCanonicalPath());
    //        hubScanConfigBuilder.addScanTargetPath(scanTarget.getParentFile().getCanonicalPath());
    //
    //        hubScanConfigBuilder.setCleanupLogsOnSuccess(true);
    //
    //        final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();
    //
    //        final ScanServiceOutput scanServiceOutput = signatureScannerService.executeScans(hubServerConfig, hubScanConfig, null);
    //        assertNotNull(scanServiceOutput);
    //        assertTrue(scanServiceOutput.getScanCommandOutputs().size() == 2);
    //
    //        for (final ScanCommandOutput scanCommandOutput : scanServiceOutput.getScanCommandOutputs()) {
    //            assertTrue(scanCommandOutput.getResult() == Result.SUCCESS);
    //            assertNotNull(scanCommandOutput.getDryRunFile());
    //        }
    //    }

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

}
