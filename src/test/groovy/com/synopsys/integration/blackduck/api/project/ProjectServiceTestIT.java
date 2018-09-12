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

import java.util.Date;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
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

        projectService.updateProjectAndVersion(project, projectRequest);

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

        projectService.updateProjectVersion(projectVersionView, projectVersionRequest);

        projectVersionView = hubService.getResponse(hubService.getHref(projectVersionView), ProjectVersionView.class);

        assertEquals("New VersionName", projectVersionView.versionName);
        assertEquals(ProjectVersionPhaseType.DEPRECATED, projectVersionView.phase);
        assertEquals(ProjectVersionDistributionType.INTERNAL, projectVersionView.distribution);
    }

}
