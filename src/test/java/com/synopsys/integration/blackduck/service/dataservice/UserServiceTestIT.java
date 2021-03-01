package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserGroupRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserRequest;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class UserServiceTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    private final BlackDuckServicesFactory BLACKDUCK_SERVICES_FACTORY = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
    private final BlackDuckApiClient BLACKDUCK_API_CLIENT = BLACKDUCK_SERVICES_FACTORY.getBlackDuckApiClient();
    private final ProjectService PROJECT_SERVICE = BLACKDUCK_SERVICES_FACTORY.createProjectService();
    private final UserGroupService USER_GROUP_SERVICE = BLACKDUCK_SERVICES_FACTORY.createUserGroupService();
    private final ProjectUsersService PROJECT_USERS_SERVICE = BLACKDUCK_SERVICES_FACTORY.createProjectUsersService();

    public UserServiceTestIT() throws IntegrationException {}

    @Test
    public void getProjectsForUserTestIT() throws IllegalArgumentException, IntegrationException {
        List<ProjectView> projectsForUser = BLACKDUCK_SERVICES_FACTORY.createUserGroupService().getProjectsForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertNotNull(projectsForUser);
    }

    @Test
    public void getRolesForUserTestIT() throws IllegalArgumentException, IntegrationException {
        List<RoleAssignmentView> rolesForUser = BLACKDUCK_SERVICES_FACTORY.createUserGroupService().getRolesForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertTrue(rolesForUser.size() > 0);
    }

    @Test
    public void testAddingGroupToProject() throws IntegrationException {
        String projectName = "user-group-project" + System.currentTimeMillis();
        String userGroupName = "user-group-test" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserGroupView userGroupView = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            PROJECT_SERVICE.syncProjectAndVersion(projectSyncModel);

            projectView = PROJECT_SERVICE.getProjectByName(projectName).get();

            UserGroupRequest userGroupRequest = new UserGroupRequest();
            userGroupRequest.setName(userGroupName);

            userGroupView = USER_GROUP_SERVICE.createUserGroup(userGroupRequest);

            List<UserGroupView> projectGroups = PROJECT_USERS_SERVICE.getGroupsForProject(projectView);
            assertFalse(projectGroups.contains(userGroupView));

            PROJECT_USERS_SERVICE.addGroupToProject(projectView, userGroupName);

            projectGroups = PROJECT_USERS_SERVICE.getGroupsForProject(projectView);
            assertTrue(projectGroups.contains(userGroupView));
        } finally {
            cleanView(projectView);
            cleanView(userGroupView);
        }
    }

    @Test
    public void testAddingUserToProject() throws IntegrationException {
        String projectName = "user-group-project" + System.currentTimeMillis();
        String userName = "user" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserView userView = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            PROJECT_SERVICE.syncProjectAndVersion(projectSyncModel);

            projectView = PROJECT_SERVICE.getProjectByName(projectName).get();

            userView = createUserView(userName, true);

            List<UserView> projectUsers = PROJECT_USERS_SERVICE.getUsersForProject(projectView);
            assertFalse(projectUsers.contains(userView));

            PROJECT_USERS_SERVICE.addUserToProject(projectView, userName);

            projectUsers = PROJECT_USERS_SERVICE.getUsersForProject(projectView);
            assertTrue(projectUsers.contains(userView));
        } finally {
            cleanView(projectView);
            cleanView(userView);
        }
    }

    @Test
    public void testInvalidUser() throws IntegrationException {
        String projectName = "user-group-project" + System.currentTimeMillis();
        String userName = "user" + System.currentTimeMillis();
        ProjectView projectView = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            PROJECT_SERVICE.syncProjectAndVersion(projectSyncModel);

            projectView = PROJECT_SERVICE.getProjectByName(projectName).orElse(null);

            ProjectView finalProjectView = projectView;
            assertThrows(BlackDuckIntegrationException.class, () -> PROJECT_USERS_SERVICE.addUserToProject(finalProjectView, userName));
        } finally {
            cleanView(projectView);
        }
    }

    @Test
    public void testGetActiveUsersForProject() throws IntegrationException {
        String projectName = "user-group-project" + System.currentTimeMillis();
        String inActiveUser = "inactive-user" + System.currentTimeMillis();
        String activeUser = "active-user" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserView inActiveUserView = null;
        UserView activeUserView = null;
        Set<UserView> allActiveUsersForProject = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            PROJECT_SERVICE.syncProjectAndVersion(projectSyncModel);
            projectView = PROJECT_SERVICE.getProjectByName(projectName).orElse(null);

            inActiveUserView = createUserView(inActiveUser, false);
            activeUserView = createUserView(activeUser, true);

            allActiveUsersForProject = PROJECT_USERS_SERVICE.getAllActiveUsersForProject(projectView);
            assertFalse(allActiveUsersForProject.contains(inActiveUserView));
            assertFalse(allActiveUsersForProject.contains(activeUserView));

            PROJECT_USERS_SERVICE.addUserToProject(projectView, inActiveUser);
            PROJECT_USERS_SERVICE.addUserToProject(projectView, activeUser);

            allActiveUsersForProject = PROJECT_USERS_SERVICE.getAllActiveUsersForProject(projectView);
            assertFalse(allActiveUsersForProject.contains(inActiveUserView));
            assertTrue(allActiveUsersForProject.contains(activeUserView));
        } finally {
            cleanView(projectView);
            cleanView(inActiveUserView);
            cleanView(activeUserView);
        }
    }

    private UserView createUserView(String userName, Boolean active) throws IntegrationException {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName(userName);
        userRequest.setFirstName("Test User");
        userRequest.setLastName("IntegrationTest");
        userRequest.setActive(active);
        userRequest.setEmail("noreply@synopsys.com");
        userRequest.setPassword("53CUR17y7hR0ugH085Cur17y");

        HttpUrl userUrl = BLACKDUCK_API_CLIENT.post(ApiDiscovery.USERS_LINK, userRequest);
        return BLACKDUCK_API_CLIENT.getResponse(userUrl, UserView.class);
    }

    private void cleanView(BlackDuckView blackDuckView) {
        if (null != blackDuckView) {
            try {
                BLACKDUCK_API_CLIENT.delete(blackDuckView);
            } catch (Exception ignored) {
                // ignored
            }
        }
    }

}
