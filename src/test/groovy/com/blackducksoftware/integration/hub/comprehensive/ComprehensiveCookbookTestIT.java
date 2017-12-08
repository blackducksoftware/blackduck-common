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
package com.blackducksoftware.integration.hub.comprehensive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blackducksoftware.integration.hub.api.bom.BomImportService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationService;
import com.blackducksoftware.integration.hub.api.item.MetaUtility;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.user.UserService;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.enumeration.CodeLocationEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.enumeration.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;

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
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final ProjectService projectRequestService = hubServicesFactory.createProjectRequestService();
        final ProjectVersionService projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService();
        final MetaUtility metaService = new MetaUtility(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection());

        // delete the project, if it exists
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName(testProjectName);
            final HubRequest deleteRequest = hubRequestFactory.createRequest(metaService.getHref(projectItem));
            deleteRequest.executeDelete();
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }

        // get the count of all projects now
        final int projectCount = projectRequestService.getAllProjects().size();

        // create the project
        final String projectUrl = projectRequestService.createHubProject(new ProjectRequest(testProjectName));
        final ProjectView projectItem = projectRequestService.getView(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = projectRequestService.getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = projectRequestService.getAllProjects().size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = projectVersionRequestService.getAllProjectVersions(projectItem).size();

        final String projectVersionUrl = projectVersionRequestService.createHubVersion(projectItem, new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, "RestConnectionTest"));
        final ProjectVersionView projectVersionItem = projectVersionRequestService.getView(projectVersionUrl, ProjectVersionView.class);
        final ProjectVersionView projectVersionItemFromName = projectVersionRequestService.getProjectVersion(projectItem, "RestConnectionTest");
        // should return the same project version
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.toString());

        assertTrue(projectVersionRequestService.getAllProjectVersions(projectItem).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final ProjectService projectRequestService = hubServicesFactory.createProjectRequestService();
        final ProjectVersionService projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService();
        final MetaUtility metaService = new MetaUtility(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection());

        // delete the project, if it exists
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName(testProjectName);
            final HubRequest deleteRequest = hubRequestFactory.createRequest(metaService.getHref(projectItem));
            deleteRequest.executeDelete();
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }

        // get the count of all projects now
        final int projectCount = projectRequestService.getAllProjects().size();

        final String versionName = "RestConnectionTest";
        final ProjectVersionDistributionEnum distribution = ProjectVersionDistributionEnum.INTERNAL;
        final ProjectVersionPhaseEnum phase = ProjectVersionPhaseEnum.DEVELOPMENT;
        final ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder();
        projectBuilder.setProjectName(testProjectName);
        projectBuilder.setVersionName(versionName);
        projectBuilder.setPhase(phase);
        projectBuilder.setDistribution(distribution);

        final ProjectRequest projectRequest = projectBuilder.build();

        // create the project
        final String projectUrl = projectRequestService.createHubProject(projectRequest);
        final ProjectView projectItem = projectRequestService.getView(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = projectRequestService.getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = projectRequestService.getAllProjects().size();
        assertTrue(projectCountAfterCreate > projectCount);

        final ProjectVersionView projectVersionItem = projectVersionRequestService.getProjectVersion(projectItem, versionName);

        assertNotNull(projectVersionItem);
        assertEquals(versionName, projectVersionItem.versionName);

    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final Date startDate = new Date();
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final ProjectService projectRequestService = hubServicesFactory.createProjectRequestService();
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection());
        final MetaUtility metaService = new MetaUtility(logger);
        final BomImportService bomImportRequestService = hubServicesFactory.createBomImportRequestService();
        final CodeLocationService codeLocationRequestService = hubServicesFactory.createCodeLocationRequestService();
        final ScanStatusDataService scanStatusDataService = hubServicesFactory.createScanStatusDataService(FIVE_MINUTES);
        final PolicyStatusDataService policyStatusDataService = hubServicesFactory.createPolicyStatusDataService();
        final NotificationService notificationRequestService = hubServicesFactory.createNotificationRequestService();

        // delete the project, if it exists
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName("ek_mtglist");
            final HubRequest deleteRequest = hubRequestFactory.createRequest(metaService.getHref(projectItem));
            deleteRequest.executeDelete();
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        bomImportRequestService.importBomFile(file, "application/ld+json");
        // wait for the scan to start/finish
        scanStatusDataService.assertBomImportScanStartedThenFinished("ek_mtglist", "0.0.1");

        // make sure we have some code locations now
        List<CodeLocationView> codeLocationItems = codeLocationRequestService.getAllCodeLocations();
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : codeLocationItems) {
                System.out.println("codeLocation: " + codeLocationItem.toString());
            }
        }
        System.out.println("Number of code locations: " + codeLocationItems.size());

        // since we imported bdio, we should also have some BOM_IMPORT code locations
        codeLocationItems = codeLocationRequestService.getAllCodeLocationsForCodeLocationType(CodeLocationEnum.BOM_IMPORT);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView item : codeLocationItems) {
                System.out.println("codeLocation: " + item.toString());
            }
        }

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = policyStatusDataService.getPolicyStatusForProjectAndVersion("ek_mtglist", "0.0.1");
        assertEquals(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);

        // TODO write a decent test for notifications
        // ejk: 2017-01-13 - until we have a better way to know when notifications are going to be created, let's knock
        // this off for now
        // right now, there is no way to test for notifications after bdio import consistently so we'll try for 10
        // minutes
        // int retryCount = 0;
        // List<NotificationItem> notifications = null;
        // while (retryCount < 10) {
        // Thread.sleep(60 * 1000);
        // final Date endDate = new Date();
        //
        // notifications = notificationRequestService.getAllNotifications(startDate, endDate);
        // if (notifications != null && !notifications.isEmpty()) {
        // break;
        // }
        // retryCount++;
        // }
        // assertTrue(notifications.size() > 0);
        // if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
        // for (final NotificationItem notificationItem : notifications) {
        // System.out.println(notificationItem);
        // }
        // }
    }

    @Test
    public void testPolicyStatusFromScan() throws Exception {
        final String projectName = restConnectionTestHelper.getProperty("TEST_SCAN_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_SCAN_VERSION");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final ProjectService projectRequestService = hubServicesFactory.createProjectRequestService();
        final MetaUtility metaService = new MetaUtility(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection());
        final CLIDataService cliDataService = hubServicesFactory.createCLIDataService(TWENTY_MINUTES);
        final PolicyStatusDataService policyStatusDataService = hubServicesFactory.createPolicyStatusDataService();

        // delete the project, if it exists
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
            final HubRequest deleteRequest = hubRequestFactory.createRequest(metaService.getHref(projectItem));
            deleteRequest.executeDelete();
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }

        try {
            projectRequestService.getProjectByName(projectName);
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

        final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(versionName);

        final ProjectRequest projectRequest = projectRequestBuilder.build();

        final ProjectVersionView version = cliDataService.installAndRunControlledScan(hubServerConfig, hubScanConfig, projectRequest, true, (String) null, null, null);

        assertNotNull(version);

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = policyStatusDataService.getPolicyStatusForVersion(version);
        assertEquals(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final ProjectService projectRequestService = hubServicesFactory.createProjectRequestService();
        final ProjectVersionService projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService();

        final List<ProjectView> allProjects = projectRequestService.getAllProjects();
        System.out.println(String.format("project count: %d", allProjects.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final ProjectView projectItem : allProjects) {
                final List<ProjectVersionView> allProjectVersions = projectVersionRequestService.getAllProjectVersions(projectItem);
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
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final CodeLocationService codeLocationRequestService = hubServicesFactory.createCodeLocationRequestService();

        final List<CodeLocationView> allCodeLocations = codeLocationRequestService.getAllCodeLocations();
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
        final UserService userRequestService = hubServicesFactory.createUserRequestService();

        final List<UserView> userItems = userRequestService.getAllUsers();
        System.out.println(String.format("user count: %d", userItems.size()));
        assertTrue(userItems != null && userItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final UserView userItem : userItems) {
                System.out.println("user: " + userItem.toString());
            }
        }
    }

}
