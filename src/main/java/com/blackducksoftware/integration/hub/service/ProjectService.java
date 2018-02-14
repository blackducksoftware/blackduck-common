/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.AssignedUserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.AssignedUserView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.MatchedFileView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.request.RequestWrapper;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.service.model.VersionBomComponentModel;
import com.blackducksoftware.integration.log.IntLogger;

public class ProjectService extends HubService {
    private final IntLogger logger;
    private final ComponentService componentDataService;

    public ProjectService(final RestConnection restConnection,
            final ComponentService componentDataService) {
        super(restConnection);
        this.logger = restConnection.logger;
        this.componentDataService = componentDataService;
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        String q = null;
        if (StringUtils.isNotBlank(projectName)) {
            q = "name:" + projectName;
        }
        final List<ProjectView> allProjectItems = getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, true, new RequestWrapper().setQ(q));
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        String q = null;
        if (StringUtils.isNotBlank(projectName)) {
            q = "name:" + projectName;
        }
        final RequestWrapper requestWrapper = new RequestWrapper().setQ(q).setLimit(limit);
        final List<ProjectView> projectItems = getResponsesFromLinkResponse(ApiDiscovery.PROJECTS_LINK_RESPONSE, false, requestWrapper);
        return projectItems;
    }

    public ProjectView getProjectByName(final String projectName) throws IntegrationException {
        final List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.name)) {
                return project;
            }
        }
        throw new DoesNotExistException("This Project does not exist. Project : " + projectName);
    }

    public String createHubProject(final ProjectRequest project) throws IntegrationException {
        return executePostRequestFromPathAndRetrieveURL(ApiDiscovery.PROJECTS_LINK, new RequestWrapper(HttpMethod.POST).setBodyContentObject(project));
    }

    public void deleteHubProject(final ProjectView project) throws IntegrationException {
        final String uri = getHref(project);
        try (Response response = executeUpdateRequest(uri, new RequestWrapper(HttpMethod.DELETE))) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public ProjectVersionView getProjectVersion(final ProjectView project, final String projectVersionName) throws IntegrationException {
        String q = "";
        if (StringUtils.isNotBlank(projectVersionName)) {
            q = String.format("versionName:%s", projectVersionName);
        }
        final List<ProjectVersionView> allProjectVersionMatchingItems = getResponsesFromLinkResponse(project, ProjectView.VERSIONS_LINK_RESPONSE, true, new RequestWrapper().setQ(q));
        for (final ProjectVersionView projectVersion : allProjectVersionMatchingItems) {
            if (projectVersionName.equals(projectVersion.versionName)) {
                return projectVersion;
            }
        }
        throw new DoesNotExistException(String.format("Could not find the version: %s for project: %s", projectVersionName, project.name));
    }

    public String createHubVersion(final ProjectView project, final ProjectVersionRequest version) throws IntegrationException {
        return createHubVersion(getFirstLink(project, ProjectView.VERSIONS_LINK), version);
    }

    public String createHubVersion(final String versionsUri, final ProjectVersionRequest version) throws IntegrationException {
        return executePostRequestAndRetrieveURL(versionsUri, new RequestWrapper(HttpMethod.POST).setBodyContentObject(version));
    }

    public ProjectVersionWrapper getProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectView, projectVersionName);

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
            project = getProjectByName(projectRequest.name);
        } catch (final DoesNotExistException e) {
            final String projectURL = createHubProject(projectRequest);
            project = getResponse(projectURL, ProjectView.class);
        }
        try {
            projectVersion = getProjectVersion(project, projectRequest.versionRequest.versionName);
        } catch (final DoesNotExistException e) {
            final String versionURL = createHubVersion(project, projectRequest.versionRequest);
            projectVersion = getResponse(versionURL, ProjectVersionView.class);
        }
        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        projectVersionWrapper.setProjectView(project);
        projectVersionWrapper.setProjectVersionView(projectVersion);
        return projectVersionWrapper;
    }

    public List<AssignedUserView> getAssignedUsersToProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getAssignedUsersToProject(project);
    }

    public List<AssignedUserView> getAssignedUsersToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserView> assignedUsers = getResponsesFromLinkResponse(project, ProjectView.USERS_LINK_RESPONSE, true);
        return assignedUsers;
    }

    public List<UserView> getUsersForProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getUsersForProject(project);
    }

    public List<UserView> getUsersForProject(final ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.name);
        final List<AssignedUserView> assignedUsers = getResponsesFromLinkResponse(project, ProjectView.USERS_LINK_RESPONSE, true);

        final List<UserView> resolvedUserViews = new ArrayList<>();
        for (final AssignedUserView assigned : assignedUsers) {
            final UserView userView = getResponse(assigned.user, UserView.class);
            if (userView != null) {
                resolvedUserViews.add(userView);
            }
        }
        return resolvedUserViews;
    }

    public List<AssignedUserGroupView> getAssignedGroupsToProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getAssignedGroupsToProject(project);
    }

    public List<AssignedUserGroupView> getAssignedGroupsToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserGroupView> assignedGroups = getResponsesFromLinkResponse(project, ProjectView.USERGROUPS_LINK_RESPONSE, true);
        return assignedGroups;
    }

    public List<UserGroupView> getGroupsForProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getGroupsForProject(project);
    }

    public List<UserGroupView> getGroupsForProject(final ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.name);
        final List<AssignedUserGroupView> assignedGroups = getResponsesFromLinkResponse(project, ProjectView.USERGROUPS_LINK_RESPONSE, true);

        final List<UserGroupView> resolvedGroupViews = new ArrayList<>();
        for (final AssignedUserGroupView assigned : assignedGroups) {
            final UserGroupView groupView = getResponse(assigned.group, UserGroupView.class);
            if (groupView != null) {
                resolvedGroupViews.add(groupView);
            }
        }
        return resolvedGroupViews;
    }

    public void addComponentToProjectVersion(final String mediaType, final String projectVersionComponentsUri, final String componentVersionUrl) throws IntegrationException {
        try (Response response = executeUpdateRequest(projectVersionComponentsUri, new RequestWrapper(HttpMethod.POST).setBodyContent("{\"component\": \"" + componentVersionUrl + "\"}"))) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void addComponentToProjectVersion(final ExternalId componentExternalId, final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectView, projectVersionName);
        final String projectVersionComponentsUrl = getFirstLink(projectVersionView, ProjectVersionView.COMPONENTS_LINK);

        final ComponentSearchResultView componentSearchResultView = componentDataService.getExactComponentMatch(componentExternalId);
        final String componentVersionUrl = componentSearchResultView.version;

        addComponentToProjectVersion("application/json", projectVersionComponentsUrl, componentVersionUrl);
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectItem, projectVersionName);
        final List<VersionBomComponentView> versionBomComponentViews = getResponsesFromLinkResponse(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE, true);
        return versionBomComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectItem, projectVersionName);
        final List<VulnerableComponentView> vulnerableBomComponentViews = getResponsesFromLinkResponse(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE, true);
        return vulnerableBomComponentViews;
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        final ProjectVersionView version = getProjectVersion(project, projectVersionName);
        return getComponentsWithMatchedFilesForProjectVersion(version);
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(final ProjectVersionView version) throws IntegrationException {
        final List<VersionBomComponentView> bomComponents = getResponsesFromLinkResponse(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE, true);
        final List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (final VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    private List<MatchedFileView> getMatchedFiles(final VersionBomComponentView component) throws IntegrationException {
        List<MatchedFileView> matchedFiles = new ArrayList<>(0);
        final List<MatchedFileView> tempMatchedFiles = getResponsesFromLinkResponseSafely(component, VersionBomComponentView.MATCHED_FILES_LINK_RESPONSE, true);
        if (tempMatchedFiles != null && tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
