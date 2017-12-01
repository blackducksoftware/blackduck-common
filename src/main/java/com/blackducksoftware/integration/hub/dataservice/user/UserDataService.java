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

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.user.UserRequestService;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.model.view.UserView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;

public class UserDataService extends HubResponseService {
    private final IntLogger logger;
    private final UserRequestService userRequestService;

    public UserDataService(final RestConnection restConnection, final UserRequestService userRequestService, final MetaService metaService) {
        super(restConnection, metaService);
        this.logger = restConnection.logger;
        this.userRequestService = userRequestService;
    }

    public List<ProjectView> getProjectsForUser(final String userName) throws IntegrationException {
        final UserView user = userRequestService.getUserByUserName(userName);
        return getProjectsForUser(user);
    }

    public List<ProjectView> getProjectsForUser(final UserView user) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + user.userName);
        return userRequestService.getUserProjects(user);
    }

    public List<RoleView> getRolesForUser(final String userName) throws IntegrationException {
        final UserView user = userRequestService.getUserByUserName(userName);
        return getRolesForUser(user);
    }

    public List<RoleView> getRolesForUser(final UserView userView) throws IntegrationException {
        return userRequestService.getUserRoles(userView);
    }

}
