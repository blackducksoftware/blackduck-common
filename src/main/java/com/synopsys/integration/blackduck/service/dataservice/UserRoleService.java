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

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.RoleAssignmentRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class UserRoleService extends DataService {
    public UserRoleService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public void addServerRoleToUser(RoleView roleView, UserView userView) throws IntegrationException {
        RoleAssignmentRequest roleAssignmentRequest = initializeRequest(roleView);
        roleAssignmentRequest.setScope(RoleService.SERVER_SCOPE);

        createUserRole(userView, roleAssignmentRequest);
    }

    public void addProjectRoleToUser(RoleView roleView, ProjectView projectView, UserView userView) throws IntegrationException {
        RoleAssignmentRequest roleAssignmentRequest = initializeRequest(roleView);
        roleAssignmentRequest.setScope(projectView.getHref().string());

        createUserRole(userView, roleAssignmentRequest);
    }

    @NotNull
    private RoleAssignmentRequest initializeRequest(RoleView roleView) {
        RoleAssignmentRequest roleAssignmentRequest = new RoleAssignmentRequest();
        roleAssignmentRequest.setRole(roleView.getHref().string());
        return roleAssignmentRequest;
    }

    private void createUserRole(UserView userView, RoleAssignmentRequest roleAssignmentRequest) throws IntegrationException {
        HttpUrl userRoleUrl = userView.getFirstLink(UserView.ROLES_LINK);
        blackDuckApiClient.post(userRoleUrl, roleAssignmentRequest);
    }

}
