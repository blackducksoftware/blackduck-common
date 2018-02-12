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
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.blackducksoftware.integration.IntegrationTest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.enumeration.CodeLocationType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusApprovalStatusType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.ScanStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.rest.UpdateRequestWrapper;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;

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
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final MetaHandler metaHandler = new MetaHandler(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection().baseUrl, hubServicesFactory.getRestConnection().gson);

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, hubRequestFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true).size();

        // create the project
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = testProjectName;
        final String projectUrl = hubServicesFactory.createProjectDataService().createHubProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = hubServicesFactory.createProjectDataService().getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final int projectVersionCount = hubServicesFactory.createHubService().getResponsesFromLinkResponse(projectItem, ProjectView.VERSIONS_LINK_RESPONSE, true).size();

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest.versionName = "RestConnectionTest";
        final String projectVersionUrl = hubServicesFactory.createProjectDataService().createHubVersion(projectItem, projectVersionRequest);
        final ProjectVersionView projectVersionItem = hubServicesFactory.createHubService().getResponse(projectVersionUrl, ProjectVersionView.class);
        final ProjectVersionView projectVersionItemFromName = hubServicesFactory.createProjectDataService().getProjectVersion(projectItem, "RestConnectionTest");
        // should return the same project version
        assertEquals(projectVersionItem.toString(), projectVersionItemFromName.toString());

        assertTrue(hubServicesFactory.createHubService().getResponsesFromLinkResponse(projectItem, ProjectView.VERSIONS_LINK_RESPONSE, true).size() > projectVersionCount);
    }

    @Test
    public void createProjectVersionSingleCall() throws Exception {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final MetaHandler metaHandler = new MetaHandler(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection().baseUrl, hubServicesFactory.getRestConnection().gson);

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, hubRequestFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true).size();

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
        final String projectUrl = hubServicesFactory.createProjectDataService().createHubProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = hubServicesFactory.createProjectDataService().getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final ProjectVersionView projectVersionItem = hubServicesFactory.createProjectDataService().getProjectVersion(projectItem, versionName);

        assertNotNull(projectVersionItem);
        assertEquals(versionName, projectVersionItem.versionName);

    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final Date startDate = new Date();
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection().baseUrl, hubServicesFactory.getRestConnection().gson);
        final MetaHandler metaHandler = new MetaHandler(logger);
        final ScanStatusDataService scanStatusDataService = hubServicesFactory.createScanStatusDataService(FIVE_MINUTES);
        final PolicyStatusDataService policyStatusDataService = hubServicesFactory.createPolicyStatusDataService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, hubRequestFactory, metaHandler, "ek_mtglist");

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        hubServicesFactory.createCodeLocationDataService().importBomFile(file, "application/ld+json");
        // wait for the scan to start/finish
        scanStatusDataService.assertBomImportScanStartedThenFinished("ek_mtglist", "0.0.1");

        // make sure we have some code locations now
        List<CodeLocationView> codeLocationItems = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, true);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView codeLocationItem : codeLocationItems) {
                System.out.println("codeLocation: " + codeLocationItem.toString());
            }
        }
        System.out.println("Number of code locations: " + codeLocationItems.size());

        // since we imported bdio, we should also have some BOM_IMPORT code locations
        codeLocationItems = hubServicesFactory.createCodeLocationDataService().getAllCodeLocationsForCodeLocationType(CodeLocationType.BOM_IMPORT);
        assertTrue(codeLocationItems != null && codeLocationItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final CodeLocationView item : codeLocationItems) {
                System.out.println("codeLocation: " + item.toString());
            }
        }

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = policyStatusDataService.getPolicyStatusForProjectAndVersion("ek_mtglist", "0.0.1");
        assertEquals(PolicyStatusApprovalStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
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
        final MetaHandler metaHandler = new MetaHandler(logger);
        final HubRequestFactory hubRequestFactory = new HubRequestFactory(hubServicesFactory.getRestConnection().baseUrl, hubServicesFactory.getRestConnection().gson);
        final CLIDataService cliDataService = hubServicesFactory.createCLIDataService(TWENTY_MINUTES);
        final PolicyStatusDataService policyStatusDataService = hubServicesFactory.createPolicyStatusDataService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, hubRequestFactory, metaHandler, projectName);

        try {
            hubServicesFactory.createProjectDataService().getProjectByName(projectName);
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

        final ProjectVersionWrapper projectVersionWrapper = cliDataService.installAndRunControlledScan(hubServerConfig, hubScanConfig, projectRequest, true, (String) null, null, null);

        assertNotNull(projectVersionWrapper);
        assertNotNull(projectVersionWrapper.getProjectView());
        assertNotNull(projectVersionWrapper.getProjectVersionView());

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = policyStatusDataService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(PolicyStatusApprovalStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final List<ProjectView> allProjects = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true);
        System.out.println(String.format("project count: %d", allProjects.size()));
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final ProjectView projectItem : allProjects) {
                final List<ProjectVersionView> allProjectVersions = hubServicesFactory.createHubService().getResponsesFromLinkResponse(projectItem, ProjectView.VERSIONS_LINK_RESPONSE, true);
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

        final List<CodeLocationView> allCodeLocations = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, true);
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

        final List<UserView> userItems = hubServicesFactory.createHubService().getResponsesFromLinkResponse(ApiDiscovery.USERS_LINK_RESPONSE, true);
        System.out.println(String.format("user count: %d", userItems.size()));
        assertTrue(userItems != null && userItems.size() > 0);
        if (Boolean.parseBoolean(restConnectionTestHelper.getProperty("LOG_DETAILS_TO_CONSOLE"))) {
            for (final UserView userItem : userItems) {
                System.out.println("user: " + userItem.toString());
            }
        }
    }

    private void deleteIfProjectExists(final IntLogger logger, final HubServicesFactory hubServicesFactory, final HubRequestFactory hubRequestFactory, final MetaHandler metaHandler, final String projectName) throws Exception {
        try {
            final ProjectView projectItem = hubServicesFactory.createProjectDataService().getProjectByName(projectName);
            final Request deleteRequest = hubRequestFactory.createRequest(metaHandler.getHref(projectItem), new UpdateRequestWrapper(HttpMethod.DELETE));
            try (Response response = hubServicesFactory.getRestConnection().executeRequest(deleteRequest)) {
            }
        } catch (final HubIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

}
