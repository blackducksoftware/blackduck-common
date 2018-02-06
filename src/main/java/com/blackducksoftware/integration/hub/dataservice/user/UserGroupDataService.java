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
package com.blackducksoftware.integration.hub.dataservice.user;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.AssignedProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.RoleView;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.user.UserService;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class UserGroupDataService extends HubService {
    private final IntLogger logger;
    private final UserService userRequestService;
    private final ProjectService projectRequestService;

    public UserGroupDataService(final RestConnection restConnection, final ProjectService projectRequestService, final UserService userRequestService) {
        super(restConnection);
        this.logger = restConnection.logger;
        this.projectRequestService = projectRequestService;
        this.userRequestService = userRequestService;
    }

    public List<ProjectView> getProjectsForUser(final String userName) throws IntegrationException {
        final UserView user = userRequestService.getUserByUserName(userName);
        return getProjectsForUser(user);
    }

    public List<ProjectView> getProjectsForUser(final UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.userName);
        final List<AssignedProjectView> assignedProjectViews = userRequestService.getUserAssignedProjects(userView);

        final List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (final AssignedProjectView assigned : assignedProjectViews) {
            final ProjectView project = projectRequestService.getResponse(assigned.project, ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleView> getRolesForUser(final String userName) throws IntegrationException {
        final UserView user = userRequestService.getUserByUserName(userName);
        return getRolesForUser(user);
    }

    public List<RoleView> getRolesForUser(final UserView userView) throws IntegrationException {
        return userRequestService.getUserRoles(userView);
    }

    public UserGroupView getGroupByName(final String groupName) throws IntegrationException {
        final List<UserGroupView> allGroups = getAllResponsesFromApi(ApiDiscovery.USERGROUPS_LINK, UserGroupView.class);
        for (final UserGroupView group : allGroups) {
            if (group.name.equalsIgnoreCase(groupName)) {
                return group;
            }
        }
        throw new DoesNotExistException("This Group does not exist. Group name : " + groupName);
    }

}
