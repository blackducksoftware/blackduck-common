/**
 * blackduck-common
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.AssignedProjectView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class UserGroupService {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;

    public UserGroupService(final BlackDuckService blackDuckService, final IntLogger logger) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
    }

    public Optional<UserView> getUserByUsername(final String username) throws IntegrationException {
        final List<UserView> allUsers = blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        for (final UserView user : allUsers) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        logger.error(String.format("The user (%s) does not exist.", username));
        return Optional.empty();
    }

    public List<ProjectView> getProjectsForUser(final String userName) throws IntegrationException {
        final Optional<UserView> user = getUserByUsername(userName);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getProjectsForUser(user.get());
    }

    public List<ProjectView> getProjectsForUser(final UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.getUserName());
        final List<AssignedProjectView> assignedProjectViews = blackDuckService.getAllResponses(userView, UserView.PROJECTS_LINK_RESPONSE);

        final List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (final AssignedProjectView assigned : assignedProjectViews) {
            final ProjectView project = blackDuckService.getResponse(assigned.getProject(), ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleAssignmentView> getRolesForUser(final String username) throws IntegrationException {
        final Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getRolesForUser(final UserView userView) throws IntegrationException {
        return blackDuckService.getAllResponses(userView, UserView.ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(final String username) throws IntegrationException {
        final Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getInheritedRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(final UserView userView) throws IntegrationException {
        return blackDuckService.getAllResponses(userView, UserView.INHERITED_ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getAllRolesForUser(final String username) throws IntegrationException {
        final Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getAllRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getAllRolesForUser(final UserView userView) throws IntegrationException {
        final Set<RoleAssignmentView> roleSet = new LinkedHashSet<>();
        roleSet.addAll(getRolesForUser(userView));
        roleSet.addAll(getInheritedRolesForUser(userView));
        return new ArrayList(roleSet);
    }

    public Optional<UserGroupView> getGroupByName(final String groupName) throws IntegrationException {
        final List<UserGroupView> allGroups = blackDuckService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
        for (final UserGroupView group : allGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return Optional.of(group);
            }
        }
        logger.error(String.format("The group (%s) does not exist.", groupName));
        return Optional.empty();
    }

}
