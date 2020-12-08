package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserGroupRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserRequest;
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

    @Test
    public void getProjectsForUserTestIT() throws IllegalArgumentException, IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();

        List<ProjectView> projectsForUser = blackDuckServicesFactory.createUserGroupService().getProjectsForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertNotNull(projectsForUser);
    }

    @Test
    public void getRolesForUserTestIT() throws IllegalArgumentException, IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();

        List<RoleAssignmentView> rolesForUser = blackDuckServicesFactory.createUserGroupService().getRolesForUser(UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername());
        assertTrue(rolesForUser.size() > 0);
    }

    @Test
    public void testAddingGroupToProject() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();

        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        UserGroupService userGroupService = blackDuckServicesFactory.createUserGroupService();
        ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();

        String projectName = "user-group-project" + System.currentTimeMillis();
        String userGroupName = "user-group-test" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserGroupView userGroupView = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            projectService.syncProjectAndVersion(projectSyncModel);

            projectView = projectService.getProjectByName(projectName).get();

            UserGroupRequest userGroupRequest = new UserGroupRequest();
            userGroupRequest.setName(userGroupName);

            userGroupView = userGroupService.createUserGroup(userGroupRequest);

            List<UserGroupView> projectGroups = projectUsersService.getGroupsForProject(projectView);
            assertFalse(projectGroups.contains(userGroupView));

            projectUsersService.addGroupToProject(projectView, userGroupName);

            projectGroups = projectUsersService.getGroupsForProject(projectView);
            assertTrue(projectGroups.contains(userGroupView));
        } finally {
            if (null != projectView) {
                try {
                    blackDuckApiClient.delete(projectView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
            if (null != userGroupView) {
                try {
                    blackDuckApiClient.delete(userGroupView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }
    }

    @Test
    public void testAddingUserToProject() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = UserServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();

        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectUsersService projectUsersService = blackDuckServicesFactory.createProjectUsersService();

        String projectName = "user-group-project" + System.currentTimeMillis();
        String userName = "user" + System.currentTimeMillis();
        ProjectView projectView = null;
        UserView userView = null;

        try {
            ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, "test");
            projectService.syncProjectAndVersion(projectSyncModel);

            projectView = projectService.getProjectByName(projectName).get();

            UserRequest userRequest = new UserRequest();
            userRequest.setUserName(userName);
            userRequest.setFirstName("Test User");
            userRequest.setLastName("IntegrationTest");
            userRequest.setActive(true);
            userRequest.setEmail("noreply@synopsys.com");
            userRequest.setPassword("53CUR17y7hR0ugH085Cur17y");

            HttpUrl userUrl = blackDuckApiClient.post(ApiDiscovery.USERS_LINK, userRequest);
            userView = blackDuckApiClient.getResponse(userUrl, UserView.class);

            List<UserView> projectUsers = projectUsersService.getUsersForProject(projectView);
            assertFalse(projectUsers.contains(userView));

            projectUsersService.addUserToProject(projectView, userName);

            projectUsers = projectUsersService.getUsersForProject(projectView);
            assertTrue(projectUsers.contains(userView));
        } finally {
            if (null != projectView) {
                try {
                    blackDuckApiClient.delete(projectView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
            if (null != userView) {
                try {
                    blackDuckApiClient.delete(userView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }
    }

}
