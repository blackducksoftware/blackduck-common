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
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PolicyStatusService;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.ScanStatusService;
import com.blackducksoftware.integration.hub.service.SignatureScannerService;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
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

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

        // create the project
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = testProjectName;
        final String projectUrl = hubServicesFactory.createProjectService().createHubProject(projectRequest);
        final ProjectView projectItem = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectView projectItemFromName = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        // should return the same project
        assertEquals(projectItem.toString(), projectItemFromName.toString());

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
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
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final MetaHandler metaHandler = new MetaHandler(logger);

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, testProjectName);

        // get the count of all projects now
        final int projectCount = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();

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

        final int projectCountAfterCreate = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.PROJECTS_LINK_RESPONSE).size();
        assertTrue(projectCountAfterCreate > projectCount);

        final ProjectVersionView projectVersionItem = hubServicesFactory.createProjectService().getProjectVersion(projectItem, versionName);

        assertNotNull(projectVersionItem);
        assertEquals(versionName, projectVersionItem.versionName);

    }

    @Test
    public void testPolicyStatusFromBdioImport() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final MetaHandler metaHandler = new MetaHandler(logger);
        final ScanStatusService scanStatusService = hubServicesFactory.createScanStatusService(FIVE_MINUTES);
        final PolicyStatusService policyStatusService = hubServicesFactory.createPolicyStatusService();

        // delete the project, if it exists
        deleteIfProjectExists(logger, hubServicesFactory, metaHandler, "ek_mtglist");

        // import the bdio
        final File file = restConnectionTestHelper.getFile("bdio/mtglist_bdio.jsonld");
        hubServicesFactory.createCodeLocationService().importBomFile(file, "application/ld+json");
        // wait for the scan to start/finish
        scanStatusService.assertBomImportScanStartedThenFinished("ek_mtglist", "0.0.1");

        // make sure we have some code locations now
        List<CodeLocationView> codeLocationItems = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
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
        final VersionBomPolicyStatusView policyStatusItem = policyStatusService.getPolicyStatusForProjectAndVersion("ek_mtglist", "0.0.1");
        assertEquals(PolicyStatusApprovalStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);
    }

    @Test
    public void testPolicyStatusFromScan() throws Exception {
        final String projectName = restConnectionTestHelper.getProperty("TEST_SCAN_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_SCAN_VERSION");

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final IntLogger logger = hubServicesFactory.getRestConnection().logger;
        final MetaHandler metaHandler = new MetaHandler(logger);
        final SignatureScannerService cliService = hubServicesFactory.createSignatureScannerService(TWENTY_MINUTES);
        final PolicyStatusService policyStatusService = hubServicesFactory.createPolicyStatusService();

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

        final HubScanConfig hubScanConfig = hubScanConfigBuilder.build();

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(versionName);

        final ProjectRequest projectRequest = projectRequestBuilder.build();

        final ProjectVersionWrapper projectVersionWrapper = cliService.installAndRunControlledScan(hubServerConfig, hubScanConfig, projectRequest, true, (String) null, null, null);

        assertNotNull(projectVersionWrapper);
        assertNotNull(projectVersionWrapper.getProjectView());
        assertNotNull(projectVersionWrapper.getProjectVersionView());

        // verify the policy
        final VersionBomPolicyStatusView policyStatusItem = policyStatusService.getPolicyStatusForVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(PolicyStatusApprovalStatusType.IN_VIOLATION, policyStatusItem.overallStatus);
        System.out.println(policyStatusItem);
    }

    @Test
    public void testGettingAllProjectsAndVersions() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final List<ProjectView> allProjects = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.PROJECTS_LINK_RESPONSE);
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

        final List<CodeLocationView> allCodeLocations = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
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

        final List<UserView> userItems = hubServicesFactory.createHubService().getAllResponsesFromPath(ApiDiscovery.USERS_LINK_RESPONSE);
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
