/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.response.AssignedProjectView;
import com.blackduck.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.blackduck.integration.blackduck.api.generated.view.UserGroupView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.temporary.component.UserGroupRequest;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class UserGroupService extends DataService {
    public static final BiPredicate<String, UserView> MATCHING_USERNAME = (username, userView) -> username.equalsIgnoreCase(userView.getUserName());

    public UserGroupService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public UserGroupView createUserGroup(UserGroupRequest userGroupRequest) throws IntegrationException {
        HttpUrl userGroupUrl = blackDuckApiClient.post(apiDiscovery.metaUsergroupsLink().getUrl(), userGroupRequest);
        UserGroupView userGroupView = blackDuckApiClient.getResponse(userGroupUrl, UserGroupView.class);
        return userGroupView;
    }

    public Optional<UserView> getUserByUsername(String username) throws IntegrationException {
        Predicate<UserView> predicate = userView -> MATCHING_USERNAME.test(username, userView);
        List<UserView> matchingUsers = blackDuckApiClient.getSomeMatchingResponses(apiDiscovery.metaUsersLink(), predicate, 1);
        if (!matchingUsers.isEmpty()) {
            return Optional.ofNullable(matchingUsers.get(0));
        }

        logger.error(String.format("The user (%s) does not exist.", username));
        return Optional.empty();
    }

    public List<ProjectView> getProjectsForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getProjectsForUser(user.get());
    }

    public List<ProjectView> getProjectsForUser(UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.getUserName());
        List<AssignedProjectView> assignedProjectViews = blackDuckApiClient.getAllResponses(userView.metaProjectsLink());

        List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (AssignedProjectView assigned : assignedProjectViews) {
            HttpUrl projectUrl = new HttpUrl(assigned.getProject());
            ProjectView project = blackDuckApiClient.getResponse(projectUrl, ProjectView.class);
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
        return blackDuckApiClient.getAllResponses(userView.metaRolesLink());
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getInheritedRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(UserView userView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(userView.metaInheritedRolesLink());
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

    public List<RoleAssignmentView> getServerRolesForUser(UserView userView) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckFilter(RoleService.createScopeFilter(
                                                                  RoleService.SERVER_SCOPE
                                                              ));

        BlackDuckMultipleRequest<RoleAssignmentView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(
            userView.metaRolesLink()
        );

        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public Optional<UserGroupView> getGroupByName(String groupName) throws IntegrationException {
        List<UserGroupView> allGroups = blackDuckApiClient.getAllResponses(apiDiscovery.metaUsergroupsLink());
        for (UserGroupView group : allGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return Optional.of(group);
            }
        }
        logger.error(String.format("The group (%s) does not exist.", groupName));
        return Optional.empty();
    }

}
