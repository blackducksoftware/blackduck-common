/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.component.AssignedUserGroupRequest;
import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectUsersService extends DataService {
    private final UserGroupService userGroupService;

    public ProjectUsersService(BlackDuckService blackDuckService, UserGroupService userGroupService, IntLogger logger) {
        super(blackDuckService, logger);
        this.userGroupService = userGroupService;
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

    public void addGroupToProject(ProjectView projectView, String groupName) throws IntegrationException {
        Optional<UserGroupView> optionalUserGroupView = userGroupService.getGroupByName(groupName);
        optionalUserGroupView.orElseThrow(() -> new IntegrationException(String.format("The supplied group name (%s) does not exist.", groupName)));

        UserGroupView userGroupView = optionalUserGroupView.get();
        List<UserGroupView> currentGroups = getGroupsForProject(projectView);
        if (currentGroups.contains(userGroupView)) {
            logger.info(String.format("The supplied project (%s) already contained the group (%s).", projectView.getName(), groupName));
            return;
        }

        userGroupView.getHref().orElseThrow(() -> new BlackDuckIntegrationException(String.format("The %s user group does not have an href so it can not be added to a project.", groupName)));

        Optional<String> projectUserGroupsLinkOptional = projectView.getFirstLink(ProjectView.USERGROUPS_LINK);
        projectUserGroupsLinkOptional.orElseThrow(() -> new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a user group.", ProjectView.USERGROUPS_LINK)));

        AssignedUserGroupRequest userGroupRequest = new AssignedUserGroupRequest();
        userGroupRequest.setGroup(userGroupView.getHref().get());
        blackDuckService.post(projectUserGroupsLinkOptional.get(), userGroupRequest);
    }

}
