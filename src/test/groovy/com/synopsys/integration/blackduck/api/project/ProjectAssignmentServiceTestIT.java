package com.synopsys.integration.blackduck.api.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
public class ProjectAssignmentServiceTestIT {
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static ProjectView project = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        if (project != null) {
            blackDuckServicesFactory.createBlackDuckService().delete(project);
        }
    }

    @Test
    public void testGetAssignedUsersFromProjectView() throws IllegalArgumentException, IntegrationException {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-common-it-ProjectAssignmentServiceTest-" + timestamp;

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        final ProjectVersionWrapper projectVersionWrapper = blackDuckServicesFactory.createProjectService().createProject(projectRequest);
        project = projectVersionWrapper.getProjectView();
        System.out.println("projectUrl: " + project.getHref().get());

        final List<AssignedUserView> assignedUsers = blackDuckServicesFactory.createProjectService().getAssignedUsersToProject(project);
        assertFalse(assignedUsers.isEmpty());
        assertEquals(1, assignedUsers.size());
        assertEquals("sysadmin", assignedUsers.get(0).getName());
    }

}
