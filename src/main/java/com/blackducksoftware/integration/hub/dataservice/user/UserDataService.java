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
package com.blackducksoftware.integration.hub.dataservice.user;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.model.view.AssignedProjectView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.model.view.UserView;

public class UserDataService {
    UserRequestService userRequestService;
    ProjectRequestService projectRequestService;
    MetaService metaService;

    public UserDataService(final UserRequestService userRequestService, final ProjectRequestService projectRequestService, final MetaService metaService) {
        this.metaService = metaService;
        this.projectRequestService = projectRequestService;
        this.userRequestService = userRequestService;
    }

    public List<ProjectView> getProjectsForUser(final String userName) throws IntegrationException {
        final UserView user = userRequestService.getUserByUserName(userName);
        return getProjectsForUser(user);
    }

    public List<ProjectView> getProjectsForUser(final UserView user) throws IntegrationException {
        final String userProjectsLink = metaService.getFirstLink(user, MetaService.PROJECTS_LINK);
        final List<AssignedProjectView> assignedProjectViews = userRequestService.getUserAssignedProjects(userProjectsLink);
        final List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (final AssignedProjectView assigned : assignedProjectViews) {
            final ProjectView project = projectRequestService.getProjectByName(assigned.name);
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
        final String userRolesLink = metaService.getFirstLink(userView, MetaService.ROLES_LINK);
        return userRequestService.getUserRoles(userRolesLink);
    }
}
