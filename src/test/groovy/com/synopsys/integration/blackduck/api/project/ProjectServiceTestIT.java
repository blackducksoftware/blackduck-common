package com.synopsys.integration.blackduck.api.project;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.core.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

@Tag("integration")
public class ProjectServiceTestIT {
    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private static BlackDuckService blackDuckService;
    private static ProjectService projectService;
    private static ProjectView project = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        projectService = blackDuckServicesFactory.createProjectService();
    }

    @AfterEach
    public void tearDownAfterTest() throws Exception {
        if (project != null) {
            blackDuckService.delete(project);
            project = null;
        }
    }

    @Test
    public void testCreateDeleteWithNickname() throws IllegalArgumentException, IntegrationException {
        final Long timestamp = (new Date()).getTime();
        final String testProjectName = "hub-common-it-ProjectServiceTest-" + timestamp;
        final String testProjectVersion1Name = "1";
        final String testProjectVersion2Name = "2";
        final String testProjectVersion3Name = "3";

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        project = projectVersionWrapper.getProjectView();
        final String projectUrl = project.getHref().get();
        System.out.println("projectUrl: " + projectUrl);

        final ProjectVersionRequest projectVersionRequest1 = new ProjectVersionRequest();
        projectVersionRequest1.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest1.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest1.setVersionName(testProjectVersion1Name);

        final ProjectVersionRequest projectVersionRequest2 = new ProjectVersionRequest();
        projectVersionRequest2.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest2.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest2.setVersionName(testProjectVersion2Name);

        final ProjectVersionRequest projectVersionRequest3 = new ProjectVersionRequest();
        projectVersionRequest3.setDistribution(ProjectVersionDistributionType.INTERNAL);
        projectVersionRequest3.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest3.setVersionName(testProjectVersion3Name);

        projectService.createProjectVersion(project, projectVersionRequest1);
        projectService.createProjectVersion(project, projectVersionRequest2);
        projectService.createProjectVersion(project, projectVersionRequest3);

        final Optional<ProjectVersionView> projectVersion1 = projectService.getProjectVersion(project, testProjectVersion1Name);
        assertEquals(testProjectVersion1Name, projectVersion1.get().getVersionName());

        final Optional<ProjectVersionView> projectVersion2 = projectService.getProjectVersion(project, testProjectVersion2Name);
        assertEquals(testProjectVersion2Name, projectVersion2.get().getVersionName());

        final Optional<ProjectVersionView> projectVersion3 = projectService.getProjectVersion(project, testProjectVersion3Name);
        assertEquals(testProjectVersion3Name, projectVersion3.get().getVersionName());

        blackDuckService.delete(project);
        project = null;

        try {
            blackDuckService.getResponse(projectUrl, ProjectView.class);
            fail("This project should have been deleted");
        } catch (final Exception e) {
            assertTrue(e instanceof BlackDuckApiException);
            assertTrue(RestConstants.NOT_FOUND_404 == ((BlackDuckApiException) e).getOriginalIntegrationRestException().getHttpStatusCode());
        }
    }

    @Test
    public void testCreateUpdateProject() throws IllegalArgumentException, IntegrationException {
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        project = projectVersionWrapper.getProjectView();
        final String projectUrl = project.getHref().get();

        assertEquals("InitialName", project.getName());
        assertTrue(2 == project.getProjectTier());
        assertEquals("Initial Description", project.getDescription());

        project.setName("New Name");
        project.setProjectTier(4);
        project.setDescription("New Description");
        blackDuckService.put(project);

        project = blackDuckService.getResponse(projectUrl, ProjectView.class);

        assertEquals("New Name", project.getName());
        assertTrue(4 == project.getProjectTier());
        assertEquals("New Description", project.getDescription());
    }

    @Test
    public void testCreateUpdateProjectVersion() throws IllegalArgumentException, IntegrationException {
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName("Initial VersionName");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.PLANNING);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.EXTERNAL);
        projectRequest.setVersionRequest(projectVersionRequest);

        final ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectRequest);
        project = projectVersionWrapper.getProjectView();
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        assertEquals("Initial VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.PLANNING, projectVersionView.getPhase());
        assertEquals(ProjectVersionDistributionType.EXTERNAL, projectVersionView.getDistribution());

        projectVersionView.setVersionName("New VersionName");
        projectVersionView.setPhase(ProjectVersionPhaseType.DEPRECATED);
        projectVersionView.setDistribution(ProjectVersionDistributionType.INTERNAL);
        blackDuckService.put(projectVersionView);

        projectVersionView = blackDuckService.getResponse(projectVersionView.getHref().get(), ProjectVersionView.class);

        assertEquals("New VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.DEPRECATED, projectVersionView.getPhase());
        assertEquals(ProjectVersionDistributionType.INTERNAL, projectVersionView.getDistribution());
    }

    @Test
    public void testCreateProjectWithTwoVersions() throws Exception {
        // first create a new project with a single version
        final String projectName = "createWithTwo" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        projectService.createProject(projectRequest);

        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.get().getProjectView();
        final List<ProjectVersionView> projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        final ProjectVersionRequest projectVersionRequest = projectRequest.getVersionRequest();
        projectVersionRequest.setVersionName("2.0.0");

        projectService.createProjectVersion(project, projectVersionRequest);

        final List<ProjectVersionView> projectVersionViewsAfterUpdate = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViewsAfterUpdate.size());
    }

    @Test
    public void testCloning() throws Exception {
        // first create a new project with a single version
        final String projectName = "create" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        projectService.createProject(projectRequest);

        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.get().getProjectView();
        final ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();

        assertNotNull(project);
        assertNotNull(projectVersionView);

        final String projectUrl = project.getHref().get();
        final String projectVersionUrl = projectVersionView.getHref().get();

        List<ProjectVersionView> projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        project.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        blackDuckService.put(project);

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setCloneFromReleaseUrl(projectVersionUrl);
        projectVersionRequest.setVersionName("1.0.0-clone");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.OPENSOURCE);
        projectService.createProjectVersion(project, projectVersionRequest);

        projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

    @Test
    public void testCreatingProjectWithoutVersion() throws Exception {
        final String projectName = "create" + Instant.now().toString();
        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(projectName);

        projectService.createProject(projectRequest);

        project = projectService.getProjectByName(projectName).get();
        assertEquals(projectName, project.getName());
    }

}
