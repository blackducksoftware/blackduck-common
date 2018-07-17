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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.AssignedProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.RoleAssignmentView;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.log.IntLogger;

public class UserGroupService {
    private final IntLogger logger;
    private final HubService hubService;

    public UserGroupService(final HubService hubService, final IntLogger logger) {
        this.logger = logger;
        this.hubService = hubService;
    }

    public UserView getUserByUserName(final String username) throws IntegrationException {
        final List<UserView> allUsers = hubService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        for (final UserView user : allUsers) {
            if (user.userName.equalsIgnoreCase(username)) {
                return user;
            }
        }
        throw new DoesNotExistException("This User does not exist. UserName : " + username);
    }

    public List<ProjectView> getProjectsForUser(final String userName) throws IntegrationException {
        final UserView user = getUserByUserName(userName);
        return getProjectsForUser(user);
    }

    public List<ProjectView> getProjectsForUser(final UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.userName);
        final List<AssignedProjectView> assignedProjectViews = hubService.getAllResponses(userView, UserView.PROJECTS_LINK_RESPONSE);

        final List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (final AssignedProjectView assigned : assignedProjectViews) {
            final ProjectView project = hubService.getResponse(assigned.project, ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleAssignmentView> getRolesForUser(final String username) throws IntegrationException {
        final UserView user = getUserByUserName(username);
        return getRolesForUser(user);
    }

    public List<RoleAssignmentView> getRolesForUser(final UserView userView) throws IntegrationException {
        return hubService.getAllResponses(userView, UserView.ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(final String username) throws IntegrationException {
        final UserView user = getUserByUserName(username);
        return getInheritedRolesForUser(user);
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(final UserView userView) throws IntegrationException {
        return hubService.getAllResponses(userView, UserView.INHERITED_ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getAllRolesForUser(final String username) throws IntegrationException {
        final UserView user = getUserByUserName(username);
        return getAllRolesForUser(user);
    }

    public List<RoleAssignmentView> getAllRolesForUser(final UserView userView) throws IntegrationException {
        final Set<RoleAssignmentView> roleSet = new LinkedHashSet<>();
        roleSet.addAll(getRolesForUser(userView));
        roleSet.addAll(getInheritedRolesForUser(userView));
        return new ArrayList(roleSet);
    }

    public UserGroupView getGroupByName(final String groupName) throws IntegrationException {
        final List<UserGroupView> allGroups = hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
        for (final UserGroupView group : allGroups) {
            if (group.name.equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        throw new DoesNotExistException("This Group does not exist. Group name : " + groupName);
    }

}
