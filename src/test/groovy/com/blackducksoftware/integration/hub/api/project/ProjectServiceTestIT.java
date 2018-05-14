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
package com.blackducksoftware.integration.hub.api.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class ProjectServiceTestIT {
    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static HubServicesFactory hubServicesFactory;
    private static ProjectView project = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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

}
