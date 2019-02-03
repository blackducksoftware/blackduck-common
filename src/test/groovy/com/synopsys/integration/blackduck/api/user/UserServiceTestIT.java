package com.synopsys.integration.blackduck.api.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.rest.TestingPropertyKey;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class UserServiceTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper(TestingPropertyKey.TEST_HTTPS_BLACK_DUCK_SERVER_URL.toString());

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
}
