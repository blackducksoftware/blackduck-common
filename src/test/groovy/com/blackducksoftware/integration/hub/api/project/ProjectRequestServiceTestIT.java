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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class ProjectRequestServiceTestIT {
    private static HubServicesFactory hubServices;

    private static ProjectRequestService projectRequestService;

    private static ProjectVersionRequestService projectVersionRequestService;

    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static ProjectView project = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        hubServices = restConnectionTestHelper.createHubServicesFactory();
        projectRequestService = hubServices.createProjectRequestService();
        projectVersionRequestService = hubServices.createProjectVersionRequestService();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (project != null) {
            projectRequestService.deleteHubProject(project);
        }
    }

    @Test
    public void testCreateDeleteWithNickname() throws IllegalArgumentException, IntegrationException {

        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-common-it-ProjectRequestServiceTest-" + timestamp;
        final String testProjectVersion1Name = "1";
        final String testProjectVersion2Name = "2";
        final String testProjectVersion3Name = "3";

        final String projectUrl = projectRequestService.createHubProject(new ProjectRequest(testProjectName));
        System.out.println("projectUrl: " + projectUrl);

        project = projectRequestService.getItem(projectUrl, ProjectView.class);
        projectVersionRequestService.createHubVersion(project, new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersion1Name));
        projectVersionRequestService.createHubVersion(project, new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersion2Name));
        projectVersionRequestService.createHubVersion(project, new ProjectVersionRequest(ProjectVersionDistributionEnum.INTERNAL, ProjectVersionPhaseEnum.DEVELOPMENT, testProjectVersion3Name));

        final ProjectVersionView projectVersion1 = projectVersionRequestService.getProjectVersion(project, testProjectVersion1Name);
        assertEquals(testProjectVersion1Name, projectVersion1.versionName);

        final ProjectVersionView projectVersion2 = projectVersionRequestService.getProjectVersion(project, testProjectVersion2Name);
        assertEquals(testProjectVersion2Name, projectVersion2.versionName);

        final ProjectVersionView projectVersion3 = projectVersionRequestService.getProjectVersion(project, testProjectVersion3Name);
        assertEquals(testProjectVersion3Name, projectVersion3.versionName);

        projectRequestService.deleteHubProject(project);
        project = null;

        try {
            project = projectRequestService.getItem(projectUrl, ProjectView.class);
            if (project != null) {
                fail("This project should have been deleted");
            }
        } catch (final IntegrationRestException e) {
            // expected
        }

    }

}
