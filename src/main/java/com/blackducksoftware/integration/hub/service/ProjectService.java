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
import java.util.Optional;

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
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.model.HubQuery;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.blackducksoftware.integration.hub.service.model.VersionBomComponentModel;
import com.blackducksoftware.integration.rest.HttpMethod;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;

public class ProjectService extends DataService {
    private final ComponentService componentDataService;

    public ProjectService(final HubService hubService, final ComponentService componentDataService) {
        super(hubService);
        this.componentDataService = componentDataService;
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);

        final List<ProjectView> allProjectItems = hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery, limit, RequestFactory.DEFAULT_OFFSET);

        final List<ProjectView> projectItems = hubService.getResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder, false);
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

    public String createHubProject(final ProjectRequest projectRequest) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(projectRequest);
        return hubService.executePostRequestAndRetrieveURL(ApiDiscovery.PROJECTS_LINK, requestBuilder);
    }

    public void deleteHubProject(final ProjectView project) throws IntegrationException {
        final String uri = hubService.getHref(project);
        final Request deleteRequest = new Request.Builder(uri).method(HttpMethod.DELETE).build();
        try (Response response = hubService.executeRequest(deleteRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public ProjectVersionView getProjectVersion(final ProjectView project, final String projectVersionName) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("versionName", projectVersionName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);

        final List<ProjectVersionView> allProjectVersionMatchingItems = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder);
        final ProjectVersionView projectVersion = findMatchingVersion(allProjectVersionMatchingItems, projectVersionName);
        if (null != projectVersion) {
            return projectVersion;
        }
        throw new DoesNotExistException(String.format("Could not find the version: %s for project: %s", projectVersionName, project.name));
    }

    public String createHubVersion(final ProjectView project, final ProjectVersionRequest versionRequest) throws IntegrationException {
        final String uri = hubService.getFirstLink(project, ProjectView.VERSIONS_LINK);
        return createHubVersion(uri, versionRequest);
    }

    public String createHubVersion(final String versionsUri, final ProjectVersionRequest versionRequest) throws IntegrationException {
        final Request request = RequestFactory.createCommonPostRequestBuilder(versionRequest).uri(versionsUri).build();
        return hubService.executePostRequestAndRetrieveURL(request);
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
            project = hubService.getResponse(projectURL, ProjectView.class);
        }
        try {
            projectVersion = getProjectVersion(project, projectRequest.versionRequest.versionName);
        } catch (final DoesNotExistException e) {
            final String versionURL = createHubVersion(project, projectRequest.versionRequest);
            projectVersion = hubService.getResponse(versionURL, ProjectVersionView.class);
        }
        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();
        projectVersionWrapper.setProjectView(project);
        projectVersionWrapper.setProjectVersionView(projectVersion);
        return projectVersionWrapper;
    }

    public void updateProjectAndVersion(final ProjectView project, ProjectRequest projectRequest) throws IntegrationException {
        String projectUri = hubService.getHref(project);
        final Request projectUpdateRequest = RequestFactory.createCommonPutRequestBuilder(projectRequest).uri(projectUri).build();
        try (Response response = hubService.executeRequest(projectUpdateRequest)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
        if (null != projectRequest.versionRequest && StringUtils.isNotBlank(projectRequest.versionRequest.versionName)) {
            ProjectVersionView projectVersionView = getProjectVersion(project, projectRequest.versionRequest.versionName);
            if (null != projectVersionView) {
                updateProjectVersion(projectVersionView, projectRequest.versionRequest);
            }
        }
    }

    public void updateProjectAndVersion(final String projectUri, ProjectRequest projectRequest) throws IntegrationException {
        updateProjectAndVersion(hubService.getResponse(projectUri, ProjectView.class), projectRequest);
    }

    public void updateProjectAndVersion(final ProjectView project, ProjectVersionView version, ProjectRequest projectRequest) throws IntegrationException {
        updateProjectAndVersion(hubService.getHref(project), hubService.getHref(version), projectRequest);
    }

    public void updateProjectAndVersion(final String projectUri, final String projectVersionUri, ProjectRequest projectRequest) throws IntegrationException {
        final Request projectUpdateRequest = RequestFactory.createCommonPutRequestBuilder(projectRequest).uri(projectUri).build();
        try (Response response = hubService.executeRequest(projectUpdateRequest)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
        updateProjectVersion(projectVersionUri, projectRequest.versionRequest);
    }

    public void updateProjectVersion(ProjectVersionView version, ProjectVersionRequest versionRequest) throws IntegrationException {
        updateProjectVersion(hubService.getHref(version), versionRequest);
    }

    public void updateProjectVersion(final String versionUri, ProjectVersionRequest versionRequest) throws IntegrationException {
        final Request projectVersionUpdateRequest = RequestFactory.createCommonPutRequestBuilder(versionRequest).uri(versionUri).build();
        try (Response response = hubService.executeRequest(projectVersionUpdateRequest)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public List<AssignedUserView> getAssignedUsersToProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getAssignedUsersToProject(project);
    }

    public List<AssignedUserView> getAssignedUsersToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserView> assignedUsers = hubService.getAllResponses(project, ProjectView.USERS_LINK_RESPONSE);
        return assignedUsers;
    }

    public List<UserView> getUsersForProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getUsersForProject(project);
    }

    public List<UserView> getUsersForProject(final ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.name);
        final List<AssignedUserView> assignedUsers = getAssignedUsersToProject(project);

        final List<UserView> resolvedUserViews = new ArrayList<>();
        for (final AssignedUserView assigned : assignedUsers) {
            final UserView userView = hubService.getResponse(assigned.user, UserView.class);
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
        final List<AssignedUserGroupView> assignedGroups = hubService.getAllResponses(project, ProjectView.USERGROUPS_LINK_RESPONSE);
        return assignedGroups;
    }

    public List<UserGroupView> getGroupsForProject(final String projectName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        return getGroupsForProject(project);
    }

    public List<UserGroupView> getGroupsForProject(final ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.name);
        final List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(project);

        final List<UserGroupView> resolvedGroupViews = new ArrayList<>();
        for (final AssignedUserGroupView assigned : assignedGroups) {
            final UserGroupView groupView = hubService.getResponse(assigned.group, UserGroupView.class);
            if (groupView != null) {
                resolvedGroupViews.add(groupView);
            }
        }
        return resolvedGroupViews;
    }

    public void addComponentToProjectVersion(final ExternalId componentExternalId, final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectView, projectVersionName);
        addComponentToProjectVersion(componentExternalId, projectVersionView);
    }

    public void addComponentToProjectVersion(final ExternalId componentExternalId, final ProjectVersionView projectVersionView) throws IntegrationException {
        final String projectVersionComponentsUrl = hubService.getFirstLink(projectVersionView, ProjectVersionView.COMPONENTS_LINK);
        final ComponentSearchResultView componentSearchResultView = componentDataService.getExactComponentMatch(componentExternalId);
        String componentVersionUrl = null;
        if (StringUtils.isNotBlank(componentSearchResultView.variant)) {
            componentVersionUrl = componentSearchResultView.variant;
        } else {
            componentVersionUrl = componentSearchResultView.version;
        }
        addComponentToProjectVersion("application/json", projectVersionComponentsUrl, componentVersionUrl);
    }

    public void addComponentToProjectVersion(final String mediaType, final String projectVersionComponentsUri, final String componentVersionUrl) throws IntegrationException {
        final Request request = RequestFactory.createCommonPostRequestBuilder("{\"component\": \"" + componentVersionUrl + "\"}").uri(projectVersionComponentsUri).mimeType(mediaType).build();
        try (Response response = hubService.executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectItem, projectVersionName);
        return getComponentsForProjectVersion(projectVersionView);
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(final ProjectVersionView projectVersionView) throws IntegrationException {
        final List<VersionBomComponentView> versionBomComponentViews = hubService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return versionBomComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectItem, projectVersionName);
        return getVulnerableComponentsForProjectVersion(projectVersionView);
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(final ProjectVersionView projectVersionView) throws IntegrationException {
        final List<VulnerableComponentView> vulnerableBomComponentViews = hubService.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = getProjectByName(projectName);
        final ProjectVersionView version = getProjectVersion(project, projectVersionName);
        return getComponentsWithMatchedFilesForProjectVersion(version);
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(final ProjectVersionView version) throws IntegrationException {
        final List<VersionBomComponentView> bomComponents = hubService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        final List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (final VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    private List<MatchedFileView> getMatchedFiles(final VersionBomComponentView component) throws IntegrationException {
        List<MatchedFileView> matchedFiles = new ArrayList<>(0);
        final List<MatchedFileView> tempMatchedFiles = hubService.getAllResponses(component, VersionBomComponentView.MATCHED_FILES_LINK_RESPONSE);
        if (tempMatchedFiles != null && tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

    public VersionBomPolicyStatusView getPolicyStatusForProjectAndVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = getProjectByName(projectName);

        final List<ProjectVersionView> projectVersions = hubService.getAllResponses(projectItem, ProjectView.VERSIONS_LINK_RESPONSE);
        final ProjectVersionView projectVersionView = findMatchingVersion(projectVersions, projectVersionName);

        return getPolicyStatusForVersion(projectVersionView);
    }

    public VersionBomPolicyStatusView getPolicyStatusForVersion(final ProjectVersionView version) throws IntegrationException {
        return hubService.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    private ProjectVersionView findMatchingVersion(final List<ProjectVersionView> projectVersions, final String projectVersionName) throws HubIntegrationException {
        for (final ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.versionName)) {
                return version;
            }
        }
        return null;
    }

}
