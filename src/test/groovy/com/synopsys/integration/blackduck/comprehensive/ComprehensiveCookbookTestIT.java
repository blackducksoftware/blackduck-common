/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.comprehensive;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.synopsys.integration.blackduck.api.view.MetaHandler;
import com.synopsys.integration.blackduck.api.view.ScanSummaryView;
import com.synopsys.integration.blackduck.cli.summary.ScanServiceOutput;
import com.synopsys.integration.blackduck.cli.summary.ScanTargetOutput;
import com.synopsys.integration.blackduck.configuration.HubScanConfig;
import com.synopsys.integration.blackduck.configuration.HubScanConfigBuilder;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.ScanStatusService;
import com.synopsys.integration.blackduck.service.SignatureScannerService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.summary.Result;
import com.synopsys.integration.hub.api.generated.component.ProjectRequest;
import com.synopsys.integration.hub.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.hub.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.hub.api.generated.enumeration.CodeLocationType;
import com.synopsys.integration.hub.api.generated.enumeration.PolicyStatusSummaryStatusType;
import com.synopsys.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.hub.api.generated.view.CodeLocationView;
import com.synopsys.integration.hub.api.generated.view.ProjectVersionView;
import com.synopsys.integration.hub.api.generated.view.ProjectView;
import com.synopsys.integration.hub.api.generated.view.UserView;
import com.synopsys.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class ComprehensiveCookbookTestIT {
    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private static final long TWENTY_MINUTES = FIVE_MINUTES * 4;

    @Rule
    public TemporaryFolder folderForCli = new TemporaryFolder();

    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void createProjectVersion() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        final MetaHandler metaHandler = new MetaHandler(logger);

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        // create the project
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = testProjectName;
        final String projectUrl = hubServicesFactory.createProjectService().createHubProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = hubServicesFactory.createHubService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size();

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest.versionName = "RestConnectionTest";
        final String projectVersionUrl = hubServicesFactory.createProjectService().createHubVersion(projectItem, projectVersionRequest);
        final ProjectVersionView projectVersionItem = hubServicesFactory.createHubService().getResponse(projectVersionUrl, ProjectVersionView.class);
        final ProjectVersionView projectVersionItemFromName = hubServicesFactory.createProjectService().getProjectVersion(projectItem, "RestConnectionTest");
        // should return the same project version
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.toString());

        assertTrue(hubServicesFactory.createHubService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        final MetaHandler metaHandler = new MetaHandler(logger);

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

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
        final String projectUrl = hubServicesFactory.createProjectService().createHubProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final ProjectVersionView projectVersionItem = hubServicesFactory.createProjectService().getProjectVersion(projectItem, versionName);

        assertNotNull(projectVersionItem);
        assertEquals(versionName, projectVersionItem.versionName);

    }

    @Ignore // this is not running reliably with our test servers. Ignoring for now but it needs to be updated to be reliable.
    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        final MetaHandler metaHandler = new MetaHandler(logger);
        final ScanStatusService scanStatusService = hubServicesFactory.createScanStatusService(FIVE_MINUTES);
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, "ek_mtglist");

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        hubServicesFactory.createCodeLocationService().importBomFile(file, "application/ld+json");
        // wait for the scan to start/finish
        scanStatusService.assertBomImportScanStartedThenFinished("ek_mtglist", "0.0.1");

        // make sure we have some code locations now
        List<CodeLocationView> codeLocationItems = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : codeLocationItems) {
                System.out.println("codeLocation: " + codeLocationItem.toString());
            }
        }
        System.out.println("Number of code locations: " + codeLocationItems.size());

        // since we imported bdio, we should also have some BOM_IMPORT code locations
        codeLocationItems = hubServicesFactory.createCodeLocationService().getAllCodeLocationsForCodeLocationType(CodeLocationType.BOM_IMPORT);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView item : codeLocationItems) {
                System.out.println("codeLocation: " + item.toString());
            }
        }

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = projectService.getPolicyStatusForProjectAndVersion("ek_mtglist", "0.0.1");
        assertEquals(PolicyStatusSummaryStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);
    }

    @Ignore // this is not running reliably with our test servers. Ignoring for now but it needs to be updated to be reliable.
    @Test
    public void testPolicyStatusFromScan() throws Exception {
        final String projectName = restConnectionTestHelper.getProperty("TEST_SCAN_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_SCAN_VERSION");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        final MetaHandler metaHandler = new MetaHandler(logger);
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        try {
            final SignatureScannerService cliService = hubServicesFactory.createSignatureScannerService(executorService);
            final ProjectService projectService = hubServicesFactory.createProjectService();

            // delete the project, if it exists
            deleteIfProjectExists(logger, hubServicesFactory, metaHandler, projectName);

            try {
                hubServicesFactory.createProjectService().getProjectByName(projectName);
                fail("The project should not exist.");
            } catch (final HubIntegrationException e) {
            }

            final HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();

            // scan the file in its parent directory
            final File scanTarget = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
            final File workingDirectory = scanTarget.getParentFile();

            final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
            hubScanConfigBuilder.setScanMemory(4096);
            hubScanConfigBuilder.setDryRun(false);
            // download the cli to where ever the file is just for convenience
            hubScanConfigBuilder.setToolsDir(workingDirectory);
            hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
            // always use the canonical path since we validate the paths by string matching
            hubScanConfigBuilder.addScanTargetPath(scanTarget.getCanonicalPath());
            hubScanConfigBuilder.setCleanupLogsOnSuccess(true);

            final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();

            final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
            projectRequestBuilder.setProjectName(projectName);
            projectRequestBuilder.setVersionName(versionName);

            final ProjectRequest projectRequest = projectRequestBuilder.build();

            final ScanServiceOutput scanServiceOutput = cliService.executeScans(hubServerConfig, hubScanConfig, projectRequest);
            assertNotNull(scanServiceOutput);
            assertTrue(scanServiceOutput.getScanTargetOutputs().size() == 1);
            final ScanTargetOutput scanTargetOutput = scanServiceOutput.getScanTargetOutputs().get(0);
            assertTrue(scanTargetOutput.getResult() == Result.SUCCESS);
            assertNotNull(scanTargetOutput.getScanSummaryView());

            final ScanSummaryView scanSummaryView = scanTargetOutput.getScanSummaryView();

            final ScanStatusService scanStatusDataService = hubServicesFactory.createScanStatusService(TWENTY_MINUTES);
            scanStatusDataService.assertScansFinished(Arrays.asList(scanSummaryView));

            assertNotNull(scanServiceOutput.getProjectVersionWrapper());
            final ProjectVersionWrapper projectVersionWrapper = scanServiceOutput.getProjectVersionWrapper();

            assertNotNull(projectVersionWrapper.getProjectView());
            assertNotNull(projectVersionWrapper.getProjectVersionView());

            // verify the policy
            final VersionBomPolicyStatusView policyStatusItem = projectService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView());
            assertEquals(PolicyStatusSummaryStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
            System.out.println(policyStatusItem);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void testMutlipleTargetScanInParallel() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        logger.setLogLevel(LogLevel.INFO);
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            final SignatureScannerService signatureScannerService = hubServicesFactory.createSignatureScannerService(executorService);

            final HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();

            // scan the file in its parent directory
            final File scanTarget = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
            final File workingDirectory = scanTarget.getParentFile();

            final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
            hubScanConfigBuilder.setScanMemory(4096);
            hubScanConfigBuilder.setDryRun(true);
            // download the cli to where ever the file is just for convenience
            hubScanConfigBuilder.setToolsDir(workingDirectory);
            hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
            // always use the canonical path since we validate the paths by string matching
            hubScanConfigBuilder.addScanTargetPath(scanTarget.getCanonicalPath());
            hubScanConfigBuilder.addScanTargetPath(scanTarget.getParentFile().getCanonicalPath());

            hubScanConfigBuilder.setCleanupLogsOnSuccess(true);

            final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();

            final ScanServiceOutput scanServiceOutput = signatureScannerService.executeScans(hubServerConfig, hubScanConfig, null);
            assertNotNull(scanServiceOutput);
            assertTrue(scanServiceOutput.getScanTargetOutputs().size() == 2);

            for (final ScanTargetOutput scanTargetOutput : scanServiceOutput.getScanTargetOutputs()) {
                assertTrue(scanTargetOutput.getResult() == Result.SUCCESS);
                assertNotNull(scanTargetOutput.getDryRunFile());
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    public void testMutlipleTargetScan() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        logger.setLogLevel(LogLevel.INFO);
        final SignatureScannerService signatureScannerService = hubServicesFactory.createSignatureScannerService();

        final HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();

        // scan the file in its parent directory
        final File scanTarget = restConnectionTestHelper.getFile("hub-artifactory-1.0.1-RC.zip");
        final File workingDirectory = scanTarget.getParentFile();

        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setScanMemory(4096);
        hubScanConfigBuilder.setDryRun(true);
        // download the cli to where ever the file is just for convenience
        hubScanConfigBuilder.setToolsDir(workingDirectory);
        hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
        // always use the canonical path since we validate the paths by string matching
        hubScanConfigBuilder.addScanTargetPath(scanTarget.getCanonicalPath());
        hubScanConfigBuilder.addScanTargetPath(scanTarget.getParentFile().getCanonicalPath());

        hubScanConfigBuilder.setCleanupLogsOnSuccess(true);

        final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();

        final ScanServiceOutput scanServiceOutput = signatureScannerService.executeScans(hubServerConfig, hubScanConfig, null);
        assertNotNull(scanServiceOutput);
        assertTrue(scanServiceOutput.getScanTargetOutputs().size() == 2);

        for (final ScanTargetOutput scanTargetOutput : scanServiceOutput.getScanTargetOutputs()) {
            assertTrue(scanTargetOutput.getResult() == Result.SUCCESS);
            assertNotNull(scanTargetOutput.getDryRunFile());
        }
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final List<ProjectView> allProjects = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
        System.out.println(String.format("project count: %d", allProjects.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final ProjectView projectItem : allProjects) {
                final List<ProjectVersionView> allProjectVersions = hubServicesFactory.createHubService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE);
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
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final List<CodeLocationView> allCodeLocations = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
        System.out.println(String.format("code location count: %d", allCodeLocations.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : allCodeLocations) {
                System.out.println(codeLocationItem.toString());
            }
        }
    }

    @Test
    public void testGettingAllUsers() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final List<UserView> userItems = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        System.out.println(String.format("user count: %d", userItems.size()));
        assertTrue(userItems != null && userItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final UserView userItem : userItems) {
                System.out.println("user: " + userItem.toString());
            }
        }
    }

    private void deleteIfProjectExists(final IntLogger logger, final HubServicesFactory hubServicesFactory, final MetaHandler metaHandler, final String projectName) throws Exception {
        try {
            final ProjectService projectService = hubServicesFactory.createProjectService();
            final ProjectView project = projectService.getProjectByName(projectName);
            projectService.deleteHubProject(project);
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

}
