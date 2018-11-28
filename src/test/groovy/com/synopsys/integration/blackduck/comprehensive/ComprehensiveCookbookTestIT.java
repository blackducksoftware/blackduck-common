package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
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
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
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
        final IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        deleteIfProjectExists(logger, blackDuckServicesFactory, testProjectName);

        // get the count of all projects now
        final int projectCount = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        // create the project
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        final String projectUrl = blackDuckServicesFactory.createProjectService().createProject(projectRequest);
        final ProjectView projectItem = blackDuckServicesFactory.createBlackDuckService().getResponse(projectUrl, ProjectView.class);
        final Optional<ProjectView> projectItemFromName = blackDuckServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = blackDuckServicesFactory.createBlackDuckService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size();

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setVersionName("RestConnectionTest");
        final Optional<String> projectVersionUrl = blackDuckServicesFactory.createProjectService().createVersion(projectItem, projectVersionRequest);
        final ProjectVersionView projectVersionItem = blackDuckServicesFactory.createBlackDuckService().getResponse(projectVersionUrl.get(), ProjectVersionView.class);
        final Optional<ProjectVersionView> projectVersionItemFromName = blackDuckServicesFactory.createProjectService().getProjectVersion(projectItem, "RestConnectionTest");
        // should return the same project version
        assertTrue(projectVersionItemFromName.isPresent());
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.get().toString());

        assertTrue(blackDuckServicesFactory.createBlackDuckService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        deleteIfProjectExists(logger, blackDuckServicesFactory, testProjectName);

        // get the count of all projects now
        final int projectCount = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

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
        final String projectUrl = blackDuckServicesFactory.createProjectService().createProject(projectRequest);
        final ProjectView projectItem = blackDuckServicesFactory.createBlackDuckService().getResponse(projectUrl, ProjectView.class);
        final Optional<ProjectView> projectItemFromName = blackDuckServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final Optional<ProjectVersionView> projectVersionItem = blackDuckServicesFactory.createProjectService().getProjectVersion(projectItem, versionName);
        assertTrue(projectVersionItem.isPresent());
        assertEquals(versionName, projectVersionItem.get().getVersionName());
    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final IntLogger logger = blackDuckServicesFactory.getLogger();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, blackDuckServicesFactory, "ek_mtglist");

        // make sure there is a policy that will be in violation
        final ExternalId externalId = new ExternalIdFactory().createMavenExternalId("org.apache.poi", "poi", "3.9");
        final ComponentService componentService = blackDuckServicesFactory.createComponentService();
        final PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        final String policyNameToDeleteLater = "Test Rule for comprehensive policy status/bdio " + System.currentTimeMillis();
        final String policyRuleUrl = policyRuleService.createPolicyRuleForExternalId(componentService, externalId, policyNameToDeleteLater);

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();

        final UploadRunner uploadRunner = new UploadRunner(logger, blackDuckService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createWithMediaType("ek_mtglist Black Duck I/O Export", file, "application/ld+json"));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        final CodeLocationCreationService codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        // make sure we have some code locations now
        final List<CodeLocationView> codeLocationItems = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
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
        final Optional<VersionBomPolicyStatusView> policyStatusItem = projectService.getPolicyStatusForProjectAndVersion("ek_mtglist", "0.0.1");
        assertTrue(policyStatusItem.isPresent());
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, policyStatusItem.get().getOverallStatus());
        System.out.println(policyStatusItem);

        final PolicyRuleViewV2 checkPolicyRule = policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
        assertNotNull(checkPolicyRule);

        blackDuckService.delete(policyRuleUrl);

        try {
            policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
            fail("Should have deleted the policy rule");
        } catch (final DoesNotExistException e) {
        }
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
    public void testGettingAllProjectsAndVersions() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final List<ProjectView> allProjects = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
        System.out.println(String.format("project count: %d", allProjects.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final ProjectView projectItem : allProjects) {
                final List<ProjectVersionView> allProjectVersions = blackDuckServicesFactory.createBlackDuckService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE);
                System.out.println(projectItem.toString());
                System.out.println(String.format("version count: %d", allProjectVersions.size()));
                for (final ProjectVersionView projectVersionItem : allProjectVersions) {
                    System.out.println(projectVersionItem.toString());
                }
            }
        }
    }

    @Test
    public void testGettingAllCodeLocations() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final List<CodeLocationView> allCodeLocations = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
        System.out.println(String.format("code location count: %d", allCodeLocations.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : allCodeLocations) {
                System.out.println(codeLocationItem.toString());
            }
        }
    }

    @Test
    public void testGettingAllUsers() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final List<UserView> userItems = blackDuckServicesFactory.createBlackDuckService().getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        System.out.println(String.format("user count: %d", userItems.size()));
        assertTrue(userItems != null && userItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final UserView userItem : userItems) {
                System.out.println("user: " + userItem.toString());
            }
        }
    }

    private void deleteIfProjectExists(final IntLogger logger, final BlackDuckServicesFactory blackDuckServicesFactory, final String projectName) throws Exception {
        try {
            final ProjectService projectService = blackDuckServicesFactory.createProjectService();
            final Optional<ProjectView> project = projectService.getProjectByName(projectName);
            if (project.isPresent()) {
                projectService.deleteProject(project.get());
            }
        } catch (final BlackDuckIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

}
