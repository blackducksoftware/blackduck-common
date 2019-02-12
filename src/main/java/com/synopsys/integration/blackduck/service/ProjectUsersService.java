package com.synopsys.integration.blackduck.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectUsersService extends DataService {
    public ProjectUsersService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
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

}
