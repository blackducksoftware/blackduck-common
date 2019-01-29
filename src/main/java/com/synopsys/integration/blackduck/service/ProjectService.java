/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
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
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.blackduck.service.model.VersionBomComponentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class ProjectService extends DataService {
    private final ProjectGetService projectGetService;
    private final ComponentService componentService;

    public ProjectService(BlackDuckService blackDuckService, IntLogger logger, ProjectGetService projectGetService, ComponentService componentService) {
        super(blackDuckService, logger);
        this.projectGetService = projectGetService;
        this.componentService = componentService;
    }

    public ProjectVersionWrapper createProject(ProjectRequest projectRequest) throws IntegrationException {
        String projectUrl = blackDuckService.post(ApiDiscovery.PROJECTS_LINK, projectRequest);
        ProjectView projectView = blackDuckService.getResponse(projectUrl, ProjectView.class);
        if (null == projectRequest.getVersionRequest()) {
            return new ProjectVersionWrapper(projectView);
        }

        Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView, projectRequest.getVersionRequest().getVersionName());
        return new ProjectVersionWrapper(projectView, projectVersionView.orElse(null));
    }

    public ProjectVersionView createProjectVersion(ProjectView projectView, ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.VERSIONS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a version.", ProjectView.VERSIONS_LINK));
        }
        String projectVersionUrl = blackDuckService.post(projectView.getFirstLink(ProjectView.VERSIONS_LINK).get(), projectVersionRequest);
        return blackDuckService.getResponse(projectVersionUrl, ProjectVersionView.class);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectRequest projectRequest) throws IntegrationException {
        return syncProjectAndVersion(projectRequest, false);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectRequest projectRequest, boolean performUpdate) throws IntegrationException {
        String projectName = projectRequest.getName();

        Optional<ProjectView> optionalProjectView = getProjectByName(projectName);
        if (!optionalProjectView.isPresent()) {
            // nothing exists, so create and return
            return createProject(projectRequest);
        }

        // the project exists, so do updating and then deal with the version
        ProjectView projectView = optionalProjectView.get();
        if (performUpdate) {
            populateViewFromRequest(projectView, projectRequest);
            blackDuckService.put(projectView);
            projectView = blackDuckService.getResponse(projectView.getHref().get(), ProjectView.class);
        }
        ProjectVersionView projectVersionView = null;

        // dealing with the version
        if (null != projectRequest.getVersionRequest() && StringUtils.isNotBlank(projectRequest.getVersionRequest().getVersionName())) {
            String projectVersionName = projectRequest.getVersionRequest().getVersionName();
            Optional<ProjectVersionView> optionalProjectVersionView = getProjectVersion(projectView, projectVersionName);
            if (optionalProjectVersionView.isPresent()) {
                // the version already exists, so do updating
                projectVersionView = optionalProjectVersionView.get();
                if (performUpdate) {
                    populateViewFromRequest(projectVersionView, projectRequest.getVersionRequest());
                    blackDuckService.put(projectVersionView);
                    projectVersionView = blackDuckService.getResponse(projectVersionView.getHref().get(), ProjectVersionView.class);
                }
            } else {
                // the version did not exist, so create it
                projectVersionView = createProjectVersion(projectView, projectRequest.getVersionRequest());
            }
        }

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

    public void populateViewFromRequest(ProjectView projectView, ProjectRequest projectRequest) {
        try {
            BeanUtils.copyProperties(projectView, projectRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Could not set properties on projectView: " + e.getMessage(), e);
        }
    }

    public void populateViewFromRequest(ProjectVersionView projectVersionView, ProjectVersionRequest projectVersionRequest) {
        try {
            BeanUtils.copyProperties(projectVersionView, projectVersionRequest);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("Could not set properties on projectVersionView: " + e.getMessage(), e);
        }
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        return projectGetService.getAllProjectMatches(projectName);
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        return projectGetService.getProjectMatches(projectName, limit);
    }

    public Optional<ProjectView> getProjectByName(String projectName) throws IntegrationException {
        return projectGetService.getProjectViewByProjectName(projectName);
    }

    public Optional<ProjectVersionView> getProjectVersion(ProjectView project, String projectVersionName) throws IntegrationException {
        return projectGetService.getProjectVersionViewByProjectVersionName(project, projectVersionName);
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(String projectName, String projectVersionName) throws IntegrationException {
        Optional<ProjectView> projectView = getProjectByName(projectName);
        if (projectView.isPresent()) {
            Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView.get(), projectVersionName);

            if (projectVersionView.isPresent()) {
                return Optional.of(new ProjectVersionWrapper(projectView.get(), projectVersionView.get()));
            }
        }

        return Optional.empty();
    }

    public List<AssignedUserView> getAssignedUsersToProject(ProjectView project) throws IntegrationException {
        List<AssignedUserView> assignedUsers = blackDuckService.getAllResponses(project, ProjectView.USERS_LINK_RESPONSE);
        return assignedUsers;
    }

    public List<UserView> getUsersForProject(ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.getName());
        List<AssignedUserView> assignedUsers = getAssignedUsersToProject(project);

        List<UserView> resolvedUserViews = new ArrayList<>();
        for (AssignedUserView assigned : assignedUsers) {
            UserView userView = blackDuckService.getResponse(assigned.getUser(), UserView.class);
            if (userView != null) {
                resolvedUserViews.add(userView);
            }
        }
        return resolvedUserViews;
    }

    public List<AssignedUserGroupView> getAssignedGroupsToProject(ProjectView project) throws IntegrationException {
        List<AssignedUserGroupView> assignedGroups = blackDuckService.getAllResponses(project, ProjectView.USERGROUPS_LINK_RESPONSE);
        return assignedGroups;
    }

    public List<UserGroupView> getGroupsForProject(ProjectView project) throws IntegrationException {
        logger.debug("Attempting to get the assigned users for Project: " + project.getName());
        List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(project);

        List<UserGroupView> resolvedGroupViews = new ArrayList<>();
        for (AssignedUserGroupView assigned : assignedGroups) {
            UserGroupView groupView = blackDuckService.getResponse(assigned.getGroup(), UserGroupView.class);
            if (groupView != null) {
                resolvedGroupViews.add(groupView);
            }
        }
        return resolvedGroupViews;
    }

    /**
     * This will get all explicitly assigned users for a project, as well as all users who are assigned to groups that are explicitly assigned to a project.
     */
    public Set<UserView> getAllActiveUsersForProject(ProjectView projectView) throws IntegrationException {
        Set<UserView> users = new HashSet<>();

        List<AssignedUserGroupView> assignedGroups = getAssignedGroupsToProject(projectView);
        for (AssignedUserGroupView assignedUserGroupView : assignedGroups) {
            if (assignedUserGroupView.getActive()) {
                UserGroupView userGroupView = blackDuckService.getResponse(assignedUserGroupView.getGroup(), UserGroupView.class);
                if (userGroupView.getActive()) {
                    List<UserView> groupUsers = blackDuckService.getAllResponses(userGroupView, UserGroupView.USERS_LINK_RESPONSE);
                    users.addAll(groupUsers);
                }
            }
        }

        List<AssignedUserView> assignedUsers = getAssignedUsersToProject(projectView);
        for (AssignedUserView assignedUser : assignedUsers) {
            UserView userView = blackDuckService.getResponse(assignedUser.getUser(), UserView.class);
            users.add(userView);
        }

        return users
                       .stream()
                       .filter(userView -> userView.getActive())
                       .collect(Collectors.toSet());
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VersionBomComponentView> versionBomComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return versionBomComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VulnerableComponentView> vulnerableBomComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VersionBomComponentView> versionBomComponentViews = getComponentsForProjectVersion(projectVersionView);
        List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (VersionBomComponentView versionBomComponentView : versionBomComponentViews) {
            if (StringUtils.isNotBlank(versionBomComponentView.getComponentVersion())) {
                ComponentVersionView componentVersionView = blackDuckService.getResponse(versionBomComponentView.getComponentVersion(), ComponentVersionView.class);
                componentVersionViews.add(componentVersionView);
            }
        }

        List<ComponentVersionVulnerabilities> componentVersionVulnerabilitiesList = new ArrayList<>();
        for (ComponentVersionView componentVersionView : componentVersionViews) {
            ComponentVersionVulnerabilities componentVersionVulnerabilities = componentService.getComponentVersionVulnerabilities(componentVersionView);
            componentVersionVulnerabilitiesList.add(componentVersionVulnerabilities);
        }
        return componentVersionVulnerabilitiesList;
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(ProjectVersionView version) throws IntegrationException {
        List<VersionBomComponentView> bomComponents = blackDuckService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    public Optional<VersionBomPolicyStatusView> getPolicyStatusForVersion(ProjectVersionView version) throws IntegrationException {
        return blackDuckService.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    public Optional<String> addComponentToProjectVersion(ExternalId componentExternalId, ProjectVersionView projectVersionView) throws IntegrationException {
        String projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK).orElse(null);
        Optional<ComponentSearchResultView> componentSearchResultView = componentService.getExactComponentMatch(componentExternalId);
        String componentVersionUrl = null;
        if (componentSearchResultView.isPresent()) {
            if (StringUtils.isNotBlank(componentSearchResultView.get().getVariant())) {
                componentVersionUrl = componentSearchResultView.get().getVariant();
            } else {
                componentVersionUrl = componentSearchResultView.get().getVersion();
            }
            addComponentToProjectVersion("application/json", projectVersionComponentsUrl, componentVersionUrl);
        }

        return Optional.ofNullable(componentVersionUrl);
    }

    public void addComponentToProjectVersion(String mediaType, String projectVersionComponentsUri, String componentVersionUrl) throws IntegrationException {
        Request request = RequestFactory.createCommonPostRequestBuilder("{\"component\": \"" + componentVersionUrl + "\"}").uri(projectVersionComponentsUri).mimeType(mediaType).build();
        try (Response response = blackDuckService.execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private List<MatchedFileView> getMatchedFiles(VersionBomComponentView component) throws IntegrationException {
        List<MatchedFileView> matchedFiles = new ArrayList<>(0);
        List<MatchedFileView> tempMatchedFiles = blackDuckService.getAllResponses(component, VersionBomComponentView.MATCHED_FILES_LINK_RESPONSE);
        if (tempMatchedFiles != null && !tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
