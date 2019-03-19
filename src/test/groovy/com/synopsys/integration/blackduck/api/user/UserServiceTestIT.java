package com.synopsys.integration.blackduck.api.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.component.UserGroupRequest;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.ProjectUsersService;
import com.synopsys.integration.blackduck.service.UserGroupService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.exception.IntegrationException;

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

        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
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
                    blackDuckService.delete(projectView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
            if (null != userGroupView) {
                try {
                    blackDuckService.delete(userGroupView);
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }
    }

}
