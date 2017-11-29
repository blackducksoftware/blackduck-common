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
package com.blackducksoftware.integration.hub.api.user;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_USERS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.view.AssignedProjectView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class UserRequestService extends HubResponseService {
    private static final List<String> USERS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_USERS);

    public UserRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<UserView> getAllUsers() throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(100, USERS_SEGMENTS);
        final List<UserView> allUserItems = getAllItems(hubPagedRequest, UserView.class);
        return allUserItems;
    }

    public UserView getUserByUserName(final String userName) throws IntegrationException {
        final List<UserView> allUsers = getAllUsers();
        for (final UserView user : allUsers) {
            if (user.userName.equalsIgnoreCase(userName)) {
                return user;
            }
        }
        throw new DoesNotExistException("This User does not exist. UserName : " + userName);
    }

    public List<ProjectView> getUserProjects(final String userProjectsLink) throws IntegrationException {
        final List<AssignedProjectView> assignedProjectViews = getAllItems(userProjectsLink, AssignedProjectView.class);

        final List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (final AssignedProjectView assigned : assignedProjectViews) {
            final ProjectView project = getItem(assigned.projectUrl, ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleView> getUserRoles(final String userRolesLink) throws IntegrationException {
        final List<RoleView> assignedRoles = this.getAllItems(userRolesLink, RoleView.class);
        return assignedRoles;
    }
}
