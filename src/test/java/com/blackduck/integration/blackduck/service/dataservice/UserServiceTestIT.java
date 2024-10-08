package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.core.BlackDuckView;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.blackduck.integration.blackduck.api.generated.view.UserGroupView;
import com.blackduck.integration.blackduck.api.generated.view.UserView;
import com.blackduck.integration.blackduck.api.manual.temporary.component.UserGroupRequest;
import com.blackduck.integration.blackduck.api.manual.temporary.component.UserRequest;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class UserServiceTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    private final BlackDuckServicesFactory blackDuckServicesFactory = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
    private final ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
    private final BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
    private final ProjectService projectService = blackDuckServicesFactory.createProjectService();
    private final UserService userService = blackDuckServicesFactory.createUserService();
    private final UserGroupService userGroupService = blackDuckServicesFactory.createUserGroupService();
    private final ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();

    private final String baseUserName = this.getClass().getSimpleName() + "_";
    private final String activeUser = baseUserName + "active-user";
    private final String inactiveUser = baseUserName + "inactive-user";

    public UserServiceTestIT() throws IntegrationException {}

    @Test
    public void getProjectsForUserTestIT() throws IllegalArgumentException, IntegrationException {
        List<ProjectView> projectsForUser = blackDuckServicesFactory.createUserGroupService().getProjectsForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertNotNull(projectsForUser);
    }

    @Test
    public void getRolesForUserTestIT() throws IllegalArgumentException, IntegrationException {
        List<RoleAssignmentView> rolesForUser = blackDuckServicesFactory.createUserGroupService().getRolesForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertTrue(rolesForUser.size() > 0);
    }

    @Test
    public void getServerRolesForUserTestIT() throws IntegrationException {
        UserGroupService service = blackDuckServicesFactory.createUserGroupService();
        String username = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername();

        UserView userView = service.getUserByUsername(username).get();

        assertNotNull(userView);

        List<RoleAssignmentView> serverRolesForUser = service.getServerRolesForUser(userView);
        assertTrue(serverRolesForUser.size() > 0);
    }

    @Test
    public void testAddingGroupToProject() throws IntegrationException {
        String userGroupName = "user-group-test" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserGroupView userGroupView = null;

        try {
            projectView = createProjectView();

            UserGroupRequest userGroupRequest = new UserGroupRequest();
            userGroupRequest.setName(userGroupName);

            userGroupView = userGroupService.createUserGroup(userGroupRequest);

            List<UserGroupView> projectGroups = projectUsersService.getGroupsForProject(projectView);
            assertFalse(projectGroups.contains(userGroupView));

            projectUsersService.addGroupToProject(projectView, userGroupName);

            projectGroups = projectUsersService.getGroupsForProject(projectView);
            assertTrue(projectGroups.contains(userGroupView));
        } finally {
            deleteView(projectView);
            deleteView(userGroupView);
        }
    }

    @Test
    public void testAddingUserToProject() throws IntegrationException {
        ProjectView projectView = null;

        try {
            projectView = createProjectView();

            UserView userView = createUserView(activeUser, true);

            List<UserView> projectUsers = projectUsersService.getUsersForProject(projectView);
            assertFalse(projectUsers.contains(userView));

            projectUsersService.addUserToProject(projectView, activeUser);

            projectUsers = projectUsersService.getUsersForProject(projectView);
            assertTrue(projectUsers.contains(userView));
        } finally {
            deleteView(projectView);
        }
    }

    @Test
    public void testInvalidUser() throws IntegrationException {
        String userName = "user-not-exist" + System.currentTimeMillis();
        ProjectView projectView = null;

        try {
            projectView = createProjectView();

            ProjectView finalProjectView = projectView;
            assertThrows(BlackDuckIntegrationException.class, () -> projectUsersService.addUserToProject(finalProjectView, userName));
        } finally {
            deleteView(projectView);
        }
    }

    @Test
    public void testGetActiveUsersForProject() throws IntegrationException {
        ProjectView projectView = null;

        try {
            projectView = createProjectView();

            UserView activeUserView = createUserView(activeUser, true);
            UserView inactiveUserView = createUserView(inactiveUser, false);

            Set<UserView> allActiveUsersForProject = projectUsersService.getAllActiveUsersForProject(projectView);
            assertFalse(allActiveUsersForProject.contains(activeUserView));
            assertFalse(allActiveUsersForProject.contains(inactiveUserView));

            projectUsersService.addUserToProject(projectView, activeUser);
            projectUsersService.addUserToProject(projectView, inactiveUser);

            allActiveUsersForProject = projectUsersService.getAllActiveUsersForProject(projectView);
            assertTrue(allActiveUsersForProject.contains(activeUserView));
            assertFalse(allActiveUsersForProject.contains(inactiveUserView));
        } finally {
            deleteView(projectView);
        }
    }

    @Test
    public void testAddSameUserToProject() throws IntegrationException {
        ProjectView projectView = null;

        try {
            projectView = createProjectView();

            UserView userView = createUserView(activeUser, true);

            List<UserView> initialUserList = projectUsersService.getUsersForProject(projectView);
            assertFalse(initialUserList.contains(userView));

            projectUsersService.addUserToProject(projectView, activeUser);

            initialUserList = projectUsersService.getUsersForProject(projectView);
            assertTrue(initialUserList.contains(userView));

            projectUsersService.addUserToProject(projectView, activeUser);

            List<UserView> finalUserList = projectUsersService.getUsersForProject(projectView);
            assertEquals(initialUserList.size(), finalUserList.size());
            assertEquals(initialUserList, finalUserList);
        } finally {
            deleteView(projectView);
        }
    }

    private UserView createUserView(String userName, Boolean active) throws IntegrationException {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName(userName);
        userRequest.setFirstName("Test User");
        userRequest.setLastName("IntegrationTest");
        userRequest.setActive(active);
        userRequest.setEmail("noreply@blackduck.com");
        userRequest.setPassword("53CUR17y7hR0ugH085Cur17y");

        UserView userView = userService.findUserByUsername(userName).orElse(null);
        if (userView == null) {
            System.out.println("Creating user " + userName);
            HttpUrl userUrl = blackDuckApiClient.post(apiDiscovery.metaUsersLink().getUrl(), userRequest);
            userView = blackDuckApiClient.getResponse(userUrl, UserView.class);
        } else {
            System.out.println(String.format("User %s already existed", userName));
        }

        return userView;
    }

    private ProjectView createProjectView() throws IntegrationException {
        String projectName = "user-group-project" + System.currentTimeMillis();
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
        projectService.syncProjectAndVersion(projectSyncModel);

        return projectService.getProjectByName(projectName).orElse(null);
    }

    private void deleteView(BlackDuckView blackDuckView) {
        if (null != blackDuckView) {
            try {
                blackDuckApiClient.delete(blackDuckView);
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

}
