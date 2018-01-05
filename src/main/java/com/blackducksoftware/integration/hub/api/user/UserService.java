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
package com.blackducksoftware.integration.hub.api.user;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_USERS;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.model.view.AssignedProjectView;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class UserService extends HubService {
    public UserService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<UserView> getAllUsers() throws IntegrationException {
        final List<UserView> allUserItems = getAllViewsFromApi(SEGMENT_USERS, UserView.class);
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

    public List<AssignedProjectView> getUserAssignedProjects(final UserView userView) throws IntegrationException {
        final List<AssignedProjectView> assignedProjectViews = getAllViewsFromLink(userView, MetaHandler.PROJECTS_LINK, AssignedProjectView.class);
        return assignedProjectViews;
    }

    public List<RoleView> getUserRoles(final UserView userView) throws IntegrationException {
        final List<RoleView> assignedRoles = this.getAllViewsFromLink(userView, MetaHandler.ROLES_LINK, RoleView.class);
        return assignedRoles;
    }

}
