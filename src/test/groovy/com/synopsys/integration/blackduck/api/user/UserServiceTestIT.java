package com.synopsys.integration.blackduck.api.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.rest.TestingPropertyKey;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
public class UserServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper(TestingPropertyKey.TEST_HTTPS_BLACK_DUCK_SERVER_URL.toString());

    @Test
    public void getProjectsForUserTestIT() throws IllegalArgumentException, IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final List<ProjectView> projectsForUser = blackDuckServicesFactory.createUserGroupService().getProjectsForUser(restConnectionTestHelper.getTestUsername());
        assertNotNull(projectsForUser);
    }

    @Test
    public void getRolesForUserTestIT() throws IllegalArgumentException, IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();

        final List<RoleAssignmentView> rolesForUser = blackDuckServicesFactory.createUserGroupService().getRolesForUser(restConnectionTestHelper.getTestUsername());
        assertTrue(rolesForUser.size() > 0);
    }
}
