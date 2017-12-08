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
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.AssignedUserView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class ProjectAssignmentRequestServiceTestIT {
    private static HubServicesFactory hubServices;
    private static ProjectService projectRequestService;
    private static ProjectAssignmentService projectAssignmentRequestService;
    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static ProjectView project = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        hubServices = restConnectionTestHelper.createHubServicesFactory();
        projectRequestService = hubServices.createProjectRequestService();
        projectAssignmentRequestService = hubServices.createProjectAssignmentRequestService();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (project != null) {
            projectRequestService.deleteHubProject(project);
        }
    }

    @Test
    public void testGetAssignedUsersFromProjectView() throws IllegalArgumentException, IntegrationException {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-common-it-ProjectAssignmentRequestServiceTest-" + timestamp;

        final String projectUrl = projectRequestService.createHubProject(new ProjectRequest(testProjectName));
        System.out.println("projectUrl: " + projectUrl);

        project = projectRequestService.getView(projectUrl, ProjectView.class);
        final List<AssignedUserView> assignedUsers = projectAssignmentRequestService.getProjectUsers(project);
        assertFalse(assignedUsers.isEmpty());
        assertEquals(1, assignedUsers.size());
        assertEquals("sysadmin", assignedUsers.get(0).name);
    }
}
