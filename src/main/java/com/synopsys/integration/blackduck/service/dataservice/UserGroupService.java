/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.UserProjectsView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.UserGroupRequest;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

import java.util.*;

public class UserGroupService extends DataService {
    public UserGroupService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger) {
        super(blackDuckService, requestFactory, logger);
    }

    public UserGroupView createUserGroup(UserGroupRequest userGroupRequest) throws IntegrationException {
        HttpUrl userGroupUrl = blackDuckService.post(ApiDiscovery.USERGROUPS_LINK, userGroupRequest);
        UserGroupView userGroupView = blackDuckService.getResponse(userGroupUrl, UserGroupView.class);
        return userGroupView;
    }

    public Optional<UserView> getUserByUsername(String username) throws IntegrationException {
        List<UserView> allUsers = blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        for (UserView user : allUsers) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        logger.error(String.format("The user (%s) does not exist.", username));
        return Optional.empty();
    }

    public List<ProjectView> getProjectsForUser(String userName) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(userName);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getProjectsForUser(user.get());
    }

    public List<ProjectView> getProjectsForUser(UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.getUserName());
        List<UserProjectsView> assignedProjectViews = blackDuckService.getAllResponses(userView, UserView.PROJECTS_LINK_RESPONSE);

        List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (UserProjectsView assigned : assignedProjectViews) {
            HttpUrl projectUrl = new HttpUrl(assigned.getProject());
            ProjectView project = blackDuckService.getResponse(projectUrl, ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleAssignmentView> getRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getRolesForUser(UserView userView) throws IntegrationException {
        return blackDuckService.getAllResponses(userView, UserView.ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getInheritedRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(UserView userView) throws IntegrationException {
        return blackDuckService.getAllResponses(userView, UserView.INHERITED_ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getAllRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getAllRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getAllRolesForUser(UserView userView) throws IntegrationException {
        Set<RoleAssignmentView> roleSet = new LinkedHashSet<>();
        roleSet.addAll(getRolesForUser(userView));
        roleSet.addAll(getInheritedRolesForUser(userView));
        return new ArrayList(roleSet);
    }

    public Optional<UserGroupView> getGroupByName(String groupName) throws IntegrationException {
        List<UserGroupView> allGroups = blackDuckService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
        for (UserGroupView group : allGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return Optional.of(group);
            }
        }
        logger.error(String.format("The group (%s) does not exist.", groupName));
        return Optional.empty();
    }

}
