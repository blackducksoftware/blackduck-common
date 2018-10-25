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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentSearchResultView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.MatchedFileView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.blackduck.service.model.VersionBomComponentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.bdio.model.externalid.ExternalId;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class ProjectService extends DataService {
    private final ProjectGetService projectGetService;
    private final ProjectUpdateService projectUpdateService;
    private final ComponentService componentDataService;

    public ProjectService(final HubService hubService, final IntLogger logger, final ProjectGetService projectGetService, final ProjectUpdateService projectUpdateService, final ComponentService componentDataService) {
        super(hubService, logger);
        this.projectGetService = projectGetService;
        this.projectUpdateService = projectUpdateService;
        this.componentDataService = componentDataService;
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        return projectGetService.getAllProjectMatches(projectName);
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        return projectGetService.getProjectMatches(projectName, limit);
    }

    public Optional<ProjectView> getProjectByName(final String projectName) throws IntegrationException {
        return projectGetService.getProjectViewByProjectName(projectName);
    }

    public Optional<ProjectVersionView> getProjectVersion(final ProjectView project, final String projectVersionName) throws IntegrationException {
        return projectGetService.getProjectVersionViewByProjectVersionName(project, projectVersionName);
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectView = getProjectByName(projectName);
        if (projectView.isPresent()) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView.get(), projectVersionName);

            if (projectVersionView.isPresent()) {
                return Optional.of(new ProjectVersionWrapper(projectView.get(), projectVersionView.get()));
            }
        }

        return Optional.empty();
    }

    public List<AssignedUserView> getAssignedUsersToProject(final String projectName) throws IntegrationException {
        final Optional<ProjectView> project = getProjectByName(projectName);
        if (project.isPresent()) {
            return getAssignedUsersToProject(project.get());
        } else {
            return Collections.emptyList();
        }
    }

    public List<AssignedUserView> getAssignedUsersToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserView> assignedUsers = hubService.getAllResponses(project, ProjectView.USERS_LINK_RESPONSE);
        return assignedUsers;
    }

    public List<UserView> getUsersForProject(final String projectName) throws IntegrationException {
        final Optional<ProjectView> project = getProjectByName(projectName);
        if (project.isPresent()) {
            return getUsersForProject(project.get());
        } else {
            return Collections.emptyList();
        }
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
        final Optional<ProjectView> project = getProjectByName(projectName);
        if (project.isPresent()) {
            return getAssignedGroupsToProject(project.get());
        } else {
            return Collections.emptyList();
        }
    }

    public List<AssignedUserGroupView> getAssignedGroupsToProject(final ProjectView project) throws IntegrationException {
        final List<AssignedUserGroupView> assignedGroups = hubService.getAllResponses(project, ProjectView.USERGROUPS_LINK_RESPONSE);
        return assignedGroups;
    }

    public List<UserGroupView> getGroupsForProject(final String projectName) throws IntegrationException {
        final Optional<ProjectView> project = getProjectByName(projectName);
        if (project.isPresent()) {
            return getGroupsForProject(project.get());
        } else {
            return Collections.emptyList();
        }
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

    /**
     * This will get all explicitly assigned users for a project, as well as all users who are assigned to groups that are explicitly assigned to a project.
     */
    public Set<UserView> getAllActiveUsersForProject(final ProjectView projectView) throws IntegrationException {
        final Set<UserView> users = new HashSet<>();

        final List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(projectView);
        for (final AssignedUserGroupView assignedUserGroupView : assignedGroups) {
            if (assignedUserGroupView.active) {
                final UserGroupView userGroupView = hubService.getResponse(assignedUserGroupView.group, UserGroupView.class);
                if (userGroupView.active) {
                    final List<UserView> groupUsers = hubService.getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                    users.addAll(groupUsers);
                }
            }
        }

        final List<AssignedUserView> assignedUsers = getAssignedUsersToProject(projectView);
        for (final AssignedUserView assignedUser : assignedUsers) {
            final UserView userView = hubService.getResponse(assignedUser.user, UserView.class);
            users.add(userView);
        }

        return users
                       .stream()
                       .filter(userView -> userView.active)
                       .collect(Collectors.toSet());
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectItem = getProjectByName(projectName);
        if (projectItem.isPresent()) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectItem.get(), projectVersionName);
            if (projectVersionView.isPresent()) {
                return getComponentsForProjectVersion(projectVersionView.get());
            }
        }

        return Collections.emptyList();
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(final ProjectVersionView projectVersionView) throws IntegrationException {
        final List<VersionBomComponentView> versionBomComponentViews = hubService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return versionBomComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectItem = getProjectByName(projectName);
        if (projectItem.isPresent()) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectItem.get(), projectVersionName);
            if (projectVersionView.isPresent()) {
                return getVulnerableComponentsForProjectVersion(projectVersionView.get());
            }
        }

        return Collections.emptyList();
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(final ProjectVersionView projectVersionView) throws IntegrationException {
        final List<VulnerableComponentView> vulnerableBomComponentViews = hubService.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(final ProjectVersionView projectVersionView) throws IntegrationException {
        final List<VersionBomComponentView> versionBomComponentViews = getComponentsForProjectVersion(projectVersionView);
        final List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (final VersionBomComponentView versionBomComponentView : versionBomComponentViews) {
            if (StringUtils.isNotBlank(versionBomComponentView.componentVersion)) {
                final ComponentVersionView componentVersionView = hubService.getResponse(versionBomComponentView.componentVersion, ComponentVersionView.class);
                componentVersionViews.add(componentVersionView);
            }
        }

        final List<ComponentVersionVulnerabilities> componentVersionVulnerabilitiesList = new ArrayList<>();
        for (final ComponentVersionView componentVersionView : componentVersionViews) {
            final ComponentVersionVulnerabilities componentVersionVulnerabilities = componentDataService.getComponentVersionVulnerabilities(componentVersionView);
            componentVersionVulnerabilitiesList.add(componentVersionVulnerabilities);
        }
        return componentVersionVulnerabilitiesList;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectVersionWrapper> projectVersionWrapper = getProjectVersion(projectName, projectVersionName);
        if (projectVersionWrapper.isPresent()) {
            final ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();
            return getComponentVersionVulnerabilities(projectVersionView);
        }
        return Collections.emptyList();
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectItem = getProjectByName(projectName);
        if (projectItem.isPresent()) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectItem.get(), projectVersionName);
            if (projectVersionView.isPresent()) {
                return getComponentsWithMatchedFilesForProjectVersion(projectVersionView.get());
            }
        }

        return Collections.emptyList();
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

    public Optional<VersionBomPolicyStatusView> getPolicyStatusForProjectAndVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectItem = getProjectByName(projectName);
        if (projectItem.isPresent()) {
            final List<ProjectVersionView> projectVersions = hubService.getAllResponses(projectItem.get(), ProjectView.VERSIONS_LINK_RESPONSE);
            final ProjectVersionView projectVersionView = findMatchingVersion(projectVersions, projectVersionName);

            return Optional.ofNullable(getPolicyStatusForVersion(projectVersionView));
        }

        return Optional.empty();
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

    public String createProject(final ProjectRequest projectRequest) throws IntegrationException {
        return projectUpdateService.createProject(projectRequest);
    }

    public void updateProject(final String projectUrl, final ProjectRequest projectRequest) throws IntegrationException {
        projectUpdateService.updateProject(projectUrl, projectRequest);
    }

    public void deleteProject(final ProjectView project) throws IntegrationException {
        final String projectUrl = hubService.getHref(project);
        projectUpdateService.deleteProject(projectUrl);
    }

    public String createVersion(final ProjectView project, final ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        final String projectVersionsUrl = hubService.getFirstLink(project, ProjectView.VERSIONS_LINK);
        return projectUpdateService.createProjectVersion(projectVersionsUrl, projectVersionRequest);
    }

    public String createVersion(final String projectVersionsUrl, final ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        return projectUpdateService.createProjectVersion(projectVersionsUrl, projectVersionRequest);
    }

    public void updateProjectVersion(final ProjectVersionView version, final ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        final String projectVersionUrl = hubService.getHref(version);
        projectUpdateService.updateProjectVersion(projectVersionUrl, projectVersionRequest);
    }

    public void updateProjectVersion(final String projectVersionUrl, final ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        projectUpdateService.updateProjectVersion(projectVersionUrl, projectVersionRequest);
    }

    public ProjectVersionWrapper syncProjectAndVersion(final ProjectRequest projectRequest) throws IntegrationException {
        return projectUpdateService.syncProjectAndVersion(projectRequest);
    }

    public ProjectVersionWrapper syncProjectAndVersion(final ProjectRequest projectRequest, final boolean performUpdate) throws IntegrationException {
        return projectUpdateService.syncProjectAndVersion(projectRequest, performUpdate);
    }

    /**
     * If a versionRequest is provided, the version will be first found by the versionName in the versionRequest and then updated.
     */
    public void updateProjectAndVersion(final String projectUri, final ProjectRequest projectRequest) throws IntegrationException {
        updateProjectAndVersion(hubService.getResponse(projectUri, ProjectView.class), projectRequest);
    }

    /**
     * If a versionRequest is provided, the version will be first found by the versionName in the versionRequest and then updated.
     */
    public void updateProjectAndVersion(final ProjectView project, final ProjectRequest projectRequest) throws IntegrationException {
        final String projectUrl = hubService.getHref(project);
        projectUpdateService.updateProject(projectUrl, projectRequest);

        if (null != projectRequest.versionRequest && StringUtils.isNotBlank(projectRequest.versionRequest.versionName)) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(project, projectRequest.versionRequest.versionName);
            if (projectVersionView.isPresent()) {
                updateProjectVersion(projectVersionView.get(), projectRequest.versionRequest);
            }
        }
    }

    public void updateProjectAndVersion(final ProjectView project, final ProjectVersionView projectVersion, final ProjectRequest projectRequest) throws IntegrationException {
        final String projectUrl = hubService.getHref(project);
        final String projectVersionUrl = hubService.getHref(projectVersion);

        updateProjectAndVersion(projectUrl, projectVersionUrl, projectRequest);
    }

    public void updateProjectAndVersion(final String projectUrl, final String projectVersionUri, final ProjectRequest projectRequest) throws IntegrationException {
        projectUpdateService.updateProject(projectUrl, projectRequest);
        updateProjectVersion(projectVersionUri, projectRequest.versionRequest);
    }

    public void addComponentToProjectVersion(final ExternalId componentExternalId, final String projectName, final String projectVersionName) throws IntegrationException {
        final Optional<ProjectView> projectItem = getProjectByName(projectName);
        if (projectItem.isPresent()) {
            final Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectItem.get(), projectVersionName);
            if (projectVersionView.isPresent()) {
                addComponentToProjectVersion(componentExternalId, projectVersionView.get());
            }
        }
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

}
