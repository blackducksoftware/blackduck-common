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

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

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
    }

    @AfterEach
    public void tearDownAfterTest() throws Exception {
        if (project != null) {
            blackDuckServicesFactory.createProjectService().deleteProject(project);
            project = null;
        }
    }

    @Test
    public void testGettingAllProjects() throws IntegrationException {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final BlackDuckPageResponse<ProjectView> projectViews = blackDuckService.getAllPageResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
        assertTrue(projectViews.getItems().size() > 0);
        assertEquals(projectViews.getTotalCount(), projectViews.getItems().size());
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
        final String projectUrl = blackDuckServicesFactory.createProjectService().createProject(projectRequest);
        System.out.println("projectUrl: " + projectUrl);

        project = blackDuckServicesFactory.createBlackDuckService().getResponse(projectUrl, ProjectView.class);
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

        blackDuckServicesFactory.createProjectService().createVersion(project, projectVersionRequest1);
        blackDuckServicesFactory.createProjectService().createVersion(project, projectVersionRequest2);
        blackDuckServicesFactory.createProjectService().createVersion(project, projectVersionRequest3);

        final Optional<ProjectVersionView> projectVersion1 = blackDuckServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion1Name);
        assertEquals(testProjectVersion1Name, projectVersion1.get().getVersionName());

        final Optional<ProjectVersionView> projectVersion2 = blackDuckServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion2Name);
        assertEquals(testProjectVersion2Name, projectVersion2.get().getVersionName());

        final Optional<ProjectVersionView> projectVersion3 = blackDuckServicesFactory.createProjectService().getProjectVersion(project, testProjectVersion3Name);
        assertEquals(testProjectVersion3Name, projectVersion3.get().getVersionName());

        blackDuckServicesFactory.createProjectService().deleteProject(project);
        project = null;

        try {
            project = blackDuckServicesFactory.createBlackDuckService().getResponse(projectUrl, ProjectView.class);
            // TODO: Expects exception. integration-rest no longer throws an exception by default
            if (project != null) {
                fail("This project should have been deleted");
            }
        } catch (final IntegrationRestException e) {
            // expected
        }
    }

    @Test
    public void testCreateUpdateProject() throws IllegalArgumentException, IntegrationException {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        final String projectUrl = projectService.createProject(projectRequest);

        project = blackDuckService.getResponse(projectUrl, ProjectView.class);

        assertEquals("InitialName", project.getName());
        assertTrue(2 == project.getProjectTier());
        assertEquals("Initial Description", project.getDescription());

        projectRequest.setName("New Name");
        projectRequest.setProjectTier(4);
        projectRequest.setDescription("New Description");

        projectService.updateProject(projectUrl, projectRequest);

        project = blackDuckService.getResponse(projectUrl, ProjectView.class);

        assertEquals("New Name", project.getName());
        assertTrue(4 == project.getProjectTier());
        assertEquals("New Description", project.getDescription());
    }

    @Test
    public void testCreateUpdateProjectVersion() throws IllegalArgumentException, IntegrationException {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName("Initial VersionName");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.PLANNING);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.EXTERNAL);
        projectRequest.setVersionRequest(projectVersionRequest);

        final String projectUrl = projectService.createProject(projectRequest);

        project = blackDuckService.getResponse(projectUrl, ProjectView.class);

        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion("InitialName", "Initial VersionName");

        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();

        assertEquals("Initial VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.PLANNING, projectVersionView.getPhase());
        assertEquals(ProjectVersionDistributionType.EXTERNAL, projectVersionView.getDistribution());

        projectVersionRequest.setVersionName("New VersionName");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEPRECATED);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.INTERNAL);

        final String projectVersionUrl = projectVersionView.getHref().get();
        projectService.updateProjectVersion(projectVersionUrl, projectVersionRequest);

        projectVersionView = blackDuckService.getResponse(projectVersionView.getHref().get(), ProjectVersionView.class);

        assertEquals("New VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.DEPRECATED, projectVersionView.getPhase());
        assertEquals(ProjectVersionDistributionType.INTERNAL, projectVersionView.getDistribution());
    }

    @Test
    public void testCreateProjectWithTwoVersions() throws Exception {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

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

        projectService.createVersion(project, projectVersionRequest);

        final List<ProjectVersionView> projectVersionViewsAfterUpdate = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViewsAfterUpdate.size());
    }

    @Test
    public void testUpdatingAProjectDoesNotAffectVersion() throws Exception {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "toBeUpdated" + Instant.now().toString();
        final String projectVersionName = "1.0.0";

        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(projectName);
        projectRequestBuilder.setVersionName(projectVersionName);
        projectRequestBuilder.setVersionNickname("first nickname");
        final ProjectRequest projectRequest = projectRequestBuilder.build();

        final String projectUrl = projectService.createProject(projectRequest);

        project = blackDuckService.getResponse(projectUrl, ProjectView.class);
        final Optional<ProjectVersionView> projectVersionView = projectService.getProjectVersion(project, projectVersionName);
        assertEquals("first nickname", projectVersionView.get().getNickname());

        final ProjectRequestBuilder updateRequestBuilder = new ProjectRequestBuilder();
        updateRequestBuilder.setFromProjectAndVersion(project, projectVersionView.get());
        updateRequestBuilder.setVersionNickname("second nickname");
        final ProjectRequest updateRequest = updateRequestBuilder.build();

        projectService.updateProject(projectUrl, updateRequest);

        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        final List<ProjectVersionView> projectVersionViews = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectView(), ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());
        assertEquals("first nickname", projectVersionViews.get(0).getNickname());
    }

    @Test
    public void testCloning() throws Exception {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

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

        projectRequestBuilder.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        projectRequestBuilder.setCloneFromReleaseUrl(projectVersionUrl);
        final ProjectRequest updateProjectRequest = projectRequestBuilder.build();
        projectService.updateProject(projectUrl, updateProjectRequest);

        final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName("1.0.0-clone");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.OPENSOURCE);
        projectService.createVersion(project, projectVersionRequest);

        projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

    @Test
    public void testSyncProjectVersionForExistingProjectWithNewVersion() throws Exception {
        final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        final ProjectService projectService = blackDuckServicesFactory.createProjectService();

        // first create a new project with a single version
        final String projectName = "syncWithTwoVersions" + Instant.now().toString();
        final String projectVersionName = "1.0.0";
        final String secondVersionName = "2.0.0";

        final ProjectRequest projectRequest = new ProjectRequestBuilder(projectName, projectVersionName).build();

        projectService.createProject(projectRequest);

        final Optional<ProjectVersionWrapper> projectVersionWrapper = projectService.getProjectVersion(projectName, projectVersionName);
        project = projectVersionWrapper.get().getProjectView();

        List<ProjectVersionView> projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        final ProjectRequest secondVersionRequest = new ProjectRequestBuilder(projectName, secondVersionName).build();
        projectService.syncProjectAndVersion(secondVersionRequest);

        projectVersionViews = blackDuckService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

}
