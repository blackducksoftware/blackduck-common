package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.blackduck.integration.blackduck.api.manual.temporary.view.AssignedUserView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ProjectAssignmentServiceTestIT {
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private final static IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();
    private static ProjectView project = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        ProjectAssignmentServiceTestIT.blackDuckServicesFactory = ProjectAssignmentServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        if (ProjectAssignmentServiceTestIT.project != null) {
            ProjectAssignmentServiceTestIT.blackDuckServicesFactory.getBlackDuckApiClient().delete(ProjectAssignmentServiceTestIT.project);
        }
    }

    @Test
    public void testGetAssignedUsersFromProjectView() throws IllegalArgumentException, IntegrationException {
        Long timestamp = (new Date()).getTime();
        String testProjectName = "hub-common-it-ProjectAssignmentServiceTest-" + timestamp;

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        ProjectVersionWrapper projectVersionWrapper = ProjectAssignmentServiceTestIT.blackDuckServicesFactory.createProjectService().createProject(projectRequest);
        ProjectAssignmentServiceTestIT.project = projectVersionWrapper.getProjectView();
        System.out.println("projectUrl: " + ProjectAssignmentServiceTestIT.project.getHref());

        List<AssignedUserView> assignedUsers = ProjectAssignmentServiceTestIT.blackDuckServicesFactory.createProjectUsersService().getAssignedUsersToProject(ProjectAssignmentServiceTestIT.project);
        assertFalse(assignedUsers.isEmpty());
        assertEquals(1, assignedUsers.size());
        assertEquals("sysadmin", assignedUsers.get(0).getName());
    }

}
