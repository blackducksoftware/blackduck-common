/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.RoleView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.temporary.component.RoleAssignmentRequest;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class UserRoleService extends DataService {
    public UserRoleService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
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
