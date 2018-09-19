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
package com.synopsys.integration.blackduck.api.project;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class ProjectServiceTestIT {
    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static HubServicesFactory hubServicesFactory;
    private static ProjectView project = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
    }

    @After
    public void tearDownAfterTest() throws Exception {
        if (project != null) {
            hubServicesFactory.createProjectService().deleteHubProject(project);
        }
    }

    @Test
    public void testCreateDeleteWithNickname() throws IllegalArgumentException, IntegrationException {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-common-it-ProjectServiceTest-" + timestamp;
        final String testProjectVersion1Name = "1";
        final String testProjectVersion2Name = "2";
        final String testProjectVersion3Name = "3";

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = testProjectName;
        final String projectUrl = hubServicesFactory.createProjectService().createHubProject(projectRequest);
        System.out.println("projectUrl: " + projectUrl);

        project = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
        final ProjectVersionRequest projectVersionRequest1 = new ProjectVersionRequest();
        projectVersionRequest1.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest1.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest1.versionName = testProjectVersion1Name;

        final ProjectVersionRequest projectVersionRequest2 = new ProjectVersionRequest();
        projectVersionRequest2.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest2.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest2.versionName = testProjectVersion2Name;

        final ProjectVersionRequest projectVersionRequest3 = new ProjectVersionRequest();
        projectVersionRequest3.distribution = ProjectVersionDistributionType.INTERNAL;
        projectVersionRequest3.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest3.versionName = testProjectVersion3Name;

        hubServicesFactory.createProjectService().createHubVersion(project, projectVersionRequest1);
        hubServicesFactory.createProjectService().createHubVersion(project, projectVersionRequest2);
        hubServicesFactory.createProjectService().createHubVersion(project, projectVersionRequest3);

        final ProjectVersionView projectVersion1 = hubServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion1Name);
        assertEquals(testProjectVersion1Name, projectVersion1.versionName);

        final ProjectVersionView projectVersion2 = hubServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion2Name);
        assertEquals(testProjectVersion2Name, projectVersion2.versionName);

        final ProjectVersionView projectVersion3 = hubServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion3Name);
        assertEquals(testProjectVersion3Name, projectVersion3.versionName);

        hubServicesFactory.createProjectService().deleteHubProject(project);
        project = null;

        try {
            project = hubServicesFactory.createHubService().getResponse(projectUrl, ProjectView.class);
            if (project != null) {
                fail("This project should have been deleted");
            }
        } catch (final IntegrationRestException e) {
            // expected
        }
    }

    @Test
    public void testCreateUpdateProject() throws IllegalArgumentException, IntegrationException {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = "InitialName";
        projectRequest.projectTier = 2;
        projectRequest.description = "Initial Description";
        final String projectUrl = projectService.createHubProject(projectRequest);

        project = hubService.getResponse(projectUrl, ProjectView.class);

        assertEquals("InitialName", project.name);
        assertTrue(2 == project.projectTier);
        assertEquals("Initial Description", project.description);

        projectRequest.name = "New Name";
        projectRequest.projectTier = 4;
        projectRequest.description = "New Description";

        projectService.updateHubProject(projectUrl, projectRequest);

        project = hubService.getResponse(projectUrl, ProjectView.class);

        assertEquals("New Name", project.name);
        assertTrue(4 == project.projectTier);
        assertEquals("New Description", project.description);
    }

    @Test
    public void testCreateUpdateProjectVersion() throws IllegalArgumentException, IntegrationException {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.name = "InitialName";
        projectRequest.projectTier = 2;
        projectRequest.description = "Initial Description";
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.versionName = "Initial VersionName";
        projectVersionRequest.phase = ProjectVersionPhaseType.PLANNING;
        projectVersionRequest.distribution = ProjectVersionDistributionType.EXTERNAL;
        projectRequest.versionRequest = projectVersionRequest;

        final String projectUrl = projectService.createHubProject(projectRequest);

        project = hubService.getResponse(projectUrl, ProjectView.class);

        final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion("InitialName", "Initial VersionName");

        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        assertEquals("Initial VersionName", projectVersionView.versionName);
        assertEquals(ProjectVersionPhaseType.PLANNING, projectVersionView.phase);
        assertEquals(ProjectVersionDistributionType.EXTERNAL, projectVersionView.distribution);

        projectVersionRequest.versionName = "New VersionName";
        projectVersionRequest.phase = ProjectVersionPhaseType.DEPRECATED;
        projectVersionRequest.distribution = ProjectVersionDistributionType.INTERNAL;

        final String projectVersionUrl = hubService.getHref(projectVersionView);
        projectService.updateProjectVersion(projectVersionUrl, projectVersionRequest);

        projectVersionView = hubService.getResponse(hubService.getHref(projectVersionView), ProjectVersionView.class);

        assertEquals("New VersionName", projectVersionView.versionName);
        assertEquals(ProjectVersionPhaseType.DEPRECATED, projectVersionView.phase);
        assertEquals(ProjectVersionDistributionType.INTERNAL, projectVersionView.distribution);
    }

    @Test
    public void testCreateProjectWithTwoVersions() throws Exception {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "createWithTwo" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        projectService.createHubProject(projectRequest);

        final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.getProjectView();
        final List<ProjectVersionView> projectVersionViews = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        final ProjectVersionRequest projectVersionRequest = projectRequest.versionRequest;
        projectVersionRequest.versionName = "2.0.0";

        projectService.createHubVersion(project, projectVersionRequest);

        final List<ProjectVersionView> projectVersionViewsAfterUpdate = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViewsAfterUpdate.size());
    }

    @Test
    public void testUpdatingAProjectDoesNotAffectVersion() throws Exception {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "toBeUpdated" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        projectRequestBuilder.setVersionNickname("first nickname");
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        final String projectUrl = projectService.createHubProject(projectRequest);

        project = hubService.getResponse(projectUrl, ProjectView.class);
        final ProjectVersionView projectVersionView = projectService.getProjectVersion(project, projectVersionName);
        assertEquals("first nickname", projectVersionView.nickname);

        final ProjectRequestBuilder updateRequestBuilder = new ProjectRequestBuilder();
        updateRequestBuilder.setFromProjectAndVersion(project, projectVersionView);
        updateRequestBuilder.setVersionNickname("second nickname");
        final ProjectRequest updateRequest = updateRequestBuilder.build();

        projectService.updateHubProject(projectUrl, updateRequest);

        final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        final List<ProjectVersionView> projectVersionViews = hubService.getAllResponses(projectVersionWrapper.getProjectView(), ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());
        assertEquals("first nickname", projectVersionViews.get(0).nickname);
    }

    @Test
    public void testCloning() throws Exception {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "create" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        projectService.createHubProject(projectRequest);

        final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        assertNotNull(project);
        assertNotNull(projectVersionView);

        final String projectUrl = hubService.getHref(project);
        final String projectVersionUrl = hubService.getHref(projectVersionView);

        List<ProjectVersionView> projectVersionViews = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        projectRequestBuilder.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        projectRequestBuilder.setCloneFromReleaseUrl(projectVersionUrl);
        final ProjectRequest updateProjectRequest = projectRequestBuilder.build();
        projectService.updateHubProject(projectUrl, updateProjectRequest);

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.versionName = "1.0.0-clone";
        projectVersionRequest.phase = ProjectVersionPhaseType.DEVELOPMENT;
        projectVersionRequest.distribution = ProjectVersionDistributionType.OPENSOURCE;
        projectService.createHubVersion(project, projectVersionRequest);

        projectVersionViews = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

    @Test
    public void testSyncProjectVersionForExistingProjectWithNewVersion() throws Exception {
        final HubService hubService = hubServicesFactory.createHubService();
        final ProjectService projectService = hubServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "syncWithTwoVersions" + Instant.now().toString();
        final String projectVersionName = "1.0.0";
        final String secondVersionName = "2.0.0";

        final ProjectRequest projectRequest = new ProjectRequestBuilder(projectName, projectVersionName).build();

        projectService.createHubProject(projectRequest);

        final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.getProjectView();

        List<ProjectVersionView> projectVersionViews = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        final ProjectRequest secondVersionRequest = new ProjectRequestBuilder(projectName, secondVersionName).build();
        projectService.syncProjectAndVersion(secondVersionRequest);

        projectVersionViews = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

}
