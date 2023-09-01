package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.temporary.view.AssignedUserView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

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
