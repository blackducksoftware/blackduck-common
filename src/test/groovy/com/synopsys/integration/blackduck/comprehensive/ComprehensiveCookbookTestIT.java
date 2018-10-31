/**
 * Hub Common
 * <p>
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

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
import com.synopsys.integration.blackduck.api.view.MetaHandler;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.log.IntLogger;
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
        final String projectUrl = hubServicesFactory.createProjectService().createProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final Optional<ProjectView> projectItemFromName = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = hubServicesFactory.createHubService().getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE).size();

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest.versionName = "RestConnectionTest";
        final String projectVersionUrl = hubServicesFactory.createProjectService().createVersion(projectItem, projectVersionRequest);
        final ProjectVersionView projectVersionItem = hubServicesFactory.createHubService().getResponse(projectVersionUrl, ProjectVersionView.class);
        final Optional<ProjectVersionView> projectVersionItemFromName = hubServicesFactory.createProjectService().getProjectVersion(projectItem, "RestConnectionTest");
        // should return the same project version
        assertTrue(projectVersionItemFromName.isPresent());
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.get().toString());

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
        final String projectUrl = hubServicesFactory.createProjectService().createProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final Optional<ProjectView> projectItemFromName = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertTrue(projectItemFromName.isPresent());
        assertEquals(projectItem.toString(), projectItemFromName.get().toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final Optional<ProjectVersionView> projectVersionItem = hubServicesFactory.createProjectService().getProjectVersion(projectItem, versionName);
        assertTrue(projectVersionItem.isPresent());
        assertEquals(versionName, projectVersionItem.get().versionName);
    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getLogger();
        final MetaHandler metaHandler = new MetaHandler(logger);
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, "ek_mtglist");

        // make sure there is a policy that will be in violation
        final ExternalId externalId = new ExternalIdFactory().createMavenExternalId("org.apache.poi", "poi", "3.9");
        final ComponentService componentService = hubServicesFactory.createComponentService();
        final PolicyRuleService policyRuleService = hubServicesFactory.createPolicyRuleService();
        final String policyNameToDeleteLater = "Test Rule for comprehensive policy status/bdio " + System.currentTimeMillis();
        final String policyRuleUrl = policyRuleService.createPolicyRuleForExternalId(componentService, externalId, policyNameToDeleteLater, metaHandler);

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        final HubService hubService = hubServicesFactory.createHubService();
        final CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService();
        final NotificationService notificationService = hubServicesFactory.createNotificationService();

        final UploadRunner uploadRunner = new UploadRunner(logger, hubService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createWithMediaType("ek_mtglist Black Duck I/O Export", file, "application/ld+json"));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        final CodeLocationCreationService codeLocationCreationService = new CodeLocationCreationService(hubService, logger, hubServicesFactory.getJsonFieldResolver(), codeLocationService, notificationService);
        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        // make sure we have some code locations now
        final List<CodeLocationView> codeLocationItems = hubServicesFactory.createHubService().getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
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
        assertEquals(PolicySummaryStatusType.IN_VIOLATION, policyStatusItem.get().overallStatus);
        System.out.println(policyStatusItem);

        final PolicyRuleViewV2 checkPolicyRule = policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
        assertNotNull(checkPolicyRule);

        hubService.delete(policyRuleUrl);

        try {
            policyRuleService.getPolicyRuleViewByName(policyNameToDeleteLater);
            fail("Should have deleted the policy rule");
        } catch (final DoesNotExistException e) {
        }
    }

    //    @Test
    //    public void testMutlipleTargetScanInParallel() throws Exception {
    //        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
    //        final IntLogger logger = hubServicesFactory.getLogger();
    //        logger.setLogLevel(LogLevel.INFO);
    //        final ExecutorService executorService = Executors.newFixedThreadPool(2);
    //        try {
    //            final SignatureScannerService signatureScannerService = hubServicesFactory.createSignatureScannerService(executorService);
    //
    //            final HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();
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
    //        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
    //        final IntLogger logger = hubServicesFactory.getLogger();
    //        logger.setLogLevel(LogLevel.INFO);
    //        final SignatureScannerService signatureScannerService = hubServicesFactory.createSignatureScannerService();
    //
    //        final HubServerConfig hubServerConfig = restConnectionTestHelper.getHubServerConfig();
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
            final Optional<ProjectView> project = projectService.getProjectByName(projectName);
            if (project.isPresent()) {
                projectService.deleteProject(project.get());
            }
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

}
