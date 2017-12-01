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
package com.blackducksoftware.integration.hub.dataservice.project;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.view.AssignedGroupView;
import com.blackducksoftware.integration.hub.model.view.AssignedUserView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;

public class ProjectDataService {
    private final ProjectRequestService projectRequestService;
    private final ProjectVersionRequestService projectVersionRequestService;
    private final ProjectAssignmentRequestService projectAssignmentRequestService;

    public ProjectDataService(final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService, final ProjectAssignmentRequestService projectAssignmentRequestService) {
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.projectAssignmentRequestService = projectAssignmentRequestService;
    }

    public ProjectVersionWrapper getProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView projectVersionView = projectVersionRequestService.getProjectVersion(projectView, projectVersionName);

        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        projectVersionWrapper.setProjectView(projectView);
        projectVersionWrapper.setProjectVersionView(projectVersionView);
        return projectVersionWrapper;
    }

    public ProjectVersionWrapper getProjectVersionAndCreateIfNeeded(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);

        final ProjectRequest projectRequest = projectRequestBuilder.build();

        return getProjectVersionAndCreateIfNeeded(projectRequest);
    }

    public ProjectVersionWrapper getProjectVersionAndCreateIfNeeded(final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null;
        ProjectVersionView projectVersion = null;

        try {
            project = projectRequestService.getProjectByName(projectRequest.getName());
        } catch (final DoesNotExistException e) {
            final String projectURL = projectRequestService.createHubProject(projectRequest);
            project = projectRequestService.getItem(projectURL, ProjectView.class);
        }

        try {
            projectVersion = projectVersionRequestService.getProjectVersion(project, projectRequest.getVersionRequest().getVersionName());
        } catch (final DoesNotExistException e) {
            final String versionURL = projectVersionRequestService.createHubVersion(project, projectRequest.getVersionRequest());
            projectVersion = projectVersionRequestService.getItem(versionURL, ProjectVersionView.class);
        }

        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        projectVersionWrapper.setProjectView(project);
        projectVersionWrapper.setProjectVersionView(projectVersion);
        return projectVersionWrapper;
    }

    public List<AssignedUserView> getAssignedUsersToProject(final String projectName) throws IntegrationException {
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        return getAssignedUsersToProject(project);
    }

    public List<AssignedUserView> getAssignedUsersToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserView> assignedUsers = projectAssignmentRequestService.getProjectUsers(project);
        return assignedUsers;
    }

    public List<AssignedGroupView> getAssignedGroupsToProject(final String projectName) throws IntegrationException {
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        return getAssignedGroupsToProject(project);
    }

    public List<AssignedGroupView> getAssignedGroupsToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedGroupView> assignedGroups = projectAssignmentRequestService.getProjectGroups(project);
        return assignedGroups;
    }

}
