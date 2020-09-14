package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ProjectServiceTestIT {
    private final static IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private static BlackDuckService blackDuckService;
    private static ProjectService projectService;
    private static ProjectView project = null;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        ProjectServiceTestIT.blackDuckServicesFactory = ProjectServiceTestIT.INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
        ProjectServiceTestIT.blackDuckService = ProjectServiceTestIT.blackDuckServicesFactory.getBlackDuckService();
        ProjectServiceTestIT.projectService = ProjectServiceTestIT.blackDuckServicesFactory.createProjectService();
    }

    @AfterEach
    public void tearDownAfterTest() throws Exception {
        if (ProjectServiceTestIT.project != null) {
            ProjectServiceTestIT.blackDuckService.delete(ProjectServiceTestIT.project);
            ProjectServiceTestIT.project = null;
        }
    }

    @Test
    public void testCreateDeleteWithNickname() throws IllegalArgumentException, IntegrationException {
        Long timestamp = (new Date()).getTime();
        String testProjectName = "hub-common-it-ProjectServiceTest-" + timestamp;
        String testProjectVersion1Name = "1";
        String testProjectVersion2Name = "2";
        String testProjectVersion3Name = "3";

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(testProjectName);
        ProjectVersionWrapper projectVersionWrapper = ProjectServiceTestIT.projectService.createProject(projectRequest);
        ProjectServiceTestIT.project = projectVersionWrapper.getProjectView();
        HttpUrl projectUrl = ProjectServiceTestIT.project.getHref();
        System.out.println("projectUrl: " + projectUrl);

        ProjectVersionRequest projectVersionRequest1 = new ProjectVersionRequest();
        projectVersionRequest1.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectVersionRequest1.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest1.setVersionName(testProjectVersion1Name);

        ProjectVersionRequest projectVersionRequest2 = new ProjectVersionRequest();
        projectVersionRequest2.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectVersionRequest2.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest2.setVersionName(testProjectVersion2Name);

        ProjectVersionRequest projectVersionRequest3 = new ProjectVersionRequest();
        projectVersionRequest3.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        projectVersionRequest3.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest3.setVersionName(testProjectVersion3Name);

        ProjectServiceTestIT.projectService.createProjectVersion(ProjectServiceTestIT.project, projectVersionRequest1);
        ProjectServiceTestIT.projectService.createProjectVersion(ProjectServiceTestIT.project, projectVersionRequest2);
        ProjectServiceTestIT.projectService.createProjectVersion(ProjectServiceTestIT.project, projectVersionRequest3);

        Optional<ProjectVersionView> projectVersion1 = ProjectServiceTestIT.projectService.getProjectVersion(ProjectServiceTestIT.project, testProjectVersion1Name);
        assertEquals(testProjectVersion1Name, projectVersion1.get().getVersionName());

        Optional<ProjectVersionView> projectVersion2 = ProjectServiceTestIT.projectService.getProjectVersion(ProjectServiceTestIT.project, testProjectVersion2Name);
        assertEquals(testProjectVersion2Name, projectVersion2.get().getVersionName());

        Optional<ProjectVersionView> projectVersion3 = ProjectServiceTestIT.projectService.getProjectVersion(ProjectServiceTestIT.project, testProjectVersion3Name);
        assertEquals(testProjectVersion3Name, projectVersion3.get().getVersionName());

        ProjectServiceTestIT.blackDuckService.delete(ProjectServiceTestIT.project);
        ProjectServiceTestIT.project = null;

        try {
            ProjectServiceTestIT.blackDuckService.getResponse(projectUrl, ProjectView.class);
            fail("This project should have been deleted");
        } catch (Exception e) {
            assertTrue(e instanceof BlackDuckApiException);
            assertTrue(RestConstants.NOT_FOUND_404 == ((BlackDuckApiException) e).getOriginalIntegrationRestException().getHttpStatusCode());
        }
    }

    @Test
    public void testCreateUpdateProject() throws IllegalArgumentException, IntegrationException {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        ProjectVersionWrapper projectVersionWrapper = ProjectServiceTestIT.projectService.createProject(projectRequest);
        ProjectServiceTestIT.project = projectVersionWrapper.getProjectView();
        HttpUrl projectUrl = ProjectServiceTestIT.project.getHref();

        assertEquals("InitialName", ProjectServiceTestIT.project.getName());
        assertTrue(2 == ProjectServiceTestIT.project.getProjectTier());
        assertEquals("Initial Description", ProjectServiceTestIT.project.getDescription());

        ProjectServiceTestIT.project.setName("New Name");
        ProjectServiceTestIT.project.setProjectTier(4);
        ProjectServiceTestIT.project.setDescription("New Description");
        ProjectServiceTestIT.blackDuckService.put(ProjectServiceTestIT.project);

        ProjectServiceTestIT.project = ProjectServiceTestIT.blackDuckService.getResponse(projectUrl, ProjectView.class);

        assertEquals("New Name", ProjectServiceTestIT.project.getName());
        assertTrue(4 == ProjectServiceTestIT.project.getProjectTier());
        assertEquals("New Description", ProjectServiceTestIT.project.getDescription());
    }

    @Test
    public void testCreateUpdateProjectVersion() throws IllegalArgumentException, IntegrationException {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName("InitialName");
        projectRequest.setProjectTier(2);
        projectRequest.setDescription("Initial Description");
        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName("Initial VersionName");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.PLANNING);
        projectVersionRequest.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.EXTERNAL);
        projectRequest.setVersionRequest(projectVersionRequest);

        ProjectVersionWrapper projectVersionWrapper = ProjectServiceTestIT.projectService.createProject(projectRequest);
        ProjectServiceTestIT.project = projectVersionWrapper.getProjectView();
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        assertEquals("Initial VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.PLANNING, projectVersionView.getPhase());
        assertEquals(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.EXTERNAL, projectVersionView.getDistribution());

        projectVersionView.setVersionName("New VersionName");
        projectVersionView.setPhase(ProjectVersionPhaseType.DEPRECATED);
        projectVersionView.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL);
        ProjectServiceTestIT.blackDuckService.put(projectVersionView);

        projectVersionView = ProjectServiceTestIT.blackDuckService.getResponse(projectVersionView.getHref(), ProjectVersionView.class);

        assertEquals("New VersionName", projectVersionView.getVersionName());
        assertEquals(ProjectVersionPhaseType.DEPRECATED, projectVersionView.getPhase());
        assertEquals(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.INTERNAL, projectVersionView.getDistribution());
    }

    @Test
    public void testCreateProjectWithTwoVersions() throws Exception {
        // first create a new project with a single version
        String projectName = "createWithTwo" + Instant.now().toString();
        String projectVersionName = "1.0.0";

        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectRequest projectRequest = projectSyncModel.createProjectRequest();

        ProjectServiceTestIT.projectService.createProject(projectRequest);

        Optional<ProjectVersionWrapper> projectVersionWrapper = ProjectServiceTestIT.projectService.getProjectVersion(projectName, projectVersionName);
        ProjectServiceTestIT.project = projectVersionWrapper.get().getProjectView();
        List<ProjectVersionView> projectVersionViews = ProjectServiceTestIT.blackDuckService.getAllResponses(ProjectServiceTestIT.project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        Optional<ProjectVersionView> latestProjectVersionView = ProjectServiceTestIT.projectService.getNewestProjectVersion(ProjectServiceTestIT.project);
        assertTrue(latestProjectVersionView.isPresent());
        assertEquals("1.0.0", latestProjectVersionView.get().getVersionName());

        ProjectVersionRequest projectVersionRequest = projectRequest.getVersionRequest();
        projectVersionRequest.setVersionName("2.0.0");

        ProjectServiceTestIT.projectService.createProjectVersion(ProjectServiceTestIT.project, projectVersionRequest);

        List<ProjectVersionView> projectVersionViewsAfterUpdate = ProjectServiceTestIT.blackDuckService.getAllResponses(ProjectServiceTestIT.project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViewsAfterUpdate.size());

        latestProjectVersionView = ProjectServiceTestIT.projectService.getNewestProjectVersion(ProjectServiceTestIT.project);
        assertTrue(latestProjectVersionView.isPresent());
        assertEquals("2.0.0", latestProjectVersionView.get().getVersionName());
    }

    @Test
    public void testCloning() throws Exception {
        // first create a new project with a single version
        String projectName = "create" + Instant.now().toString();
        String projectVersionName = "1.0.0";

        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectRequest projectRequest = projectSyncModel.createProjectRequest();

        ProjectServiceTestIT.projectService.createProject(projectRequest);

        Optional<ProjectVersionWrapper> projectVersionWrapper = ProjectServiceTestIT.projectService.getProjectVersion(projectName, projectVersionName);
        ProjectServiceTestIT.project = projectVersionWrapper.get().getProjectView();
        ProjectVersionView projectVersionView = projectVersionWrapper.get().getProjectVersionView();

        assertNotNull(ProjectServiceTestIT.project);
        assertNotNull(projectVersionView);

        HttpUrl projectUrl = ProjectServiceTestIT.project.getHref();
        HttpUrl projectVersionUrl = projectVersionView.getHref();

        List<ProjectVersionView> projectVersionViews = ProjectServiceTestIT.blackDuckService.getAllResponses(ProjectServiceTestIT.project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(1, projectVersionViews.size());

        ProjectServiceTestIT.project.setCloneCategories(Arrays.asList(ProjectCloneCategoriesType.COMPONENT_DATA));
        ProjectServiceTestIT.blackDuckService.put(ProjectServiceTestIT.project);

        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setCloneFromReleaseUrl(projectVersionUrl.string());
        projectVersionRequest.setVersionName("1.0.0-clone");
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setDistribution(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.OPENSOURCE);
        ProjectServiceTestIT.projectService.createProjectVersion(ProjectServiceTestIT.project, projectVersionRequest);

        projectVersionViews = ProjectServiceTestIT.blackDuckService.getAllResponses(ProjectServiceTestIT.project, ProjectView.VERSIONS_LINK_RESPONSE);
        assertEquals(2, projectVersionViews.size());
    }

    @Test
    public void testCreatingProjectWithoutVersion() throws Exception {
        String projectName = "create" + Instant.now().toString();
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(projectName);

        ProjectServiceTestIT.projectService.createProject(projectRequest);

        ProjectServiceTestIT.project = ProjectServiceTestIT.projectService.getProjectByName(projectName).get();
        assertEquals(projectName, ProjectServiceTestIT.project.getName());
    }

    @Test
    public void testProjectSyncModelUpdatesSetFieldsOnly() throws IntegrationException {
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults("testSync", "testSyncVersion");
        projectSyncModel.setProjectTier(2);
        projectSyncModel.setDescription("original");
        projectSyncModel.setNickname("panda bear");

        ProjectVersionWrapper projectVersionWrapper = ProjectServiceTestIT.projectService.syncProjectAndVersion(projectSyncModel, true);
        ProjectServiceTestIT.project = projectVersionWrapper.getProjectView();

        ProjectSyncModel updateProjectSyncModel = new ProjectSyncModel("testSync", "testSyncVersion");
        ProjectVersionWrapper firstUpdate = ProjectServiceTestIT.projectService.syncProjectAndVersion(updateProjectSyncModel, true);
        assertEquals(new Integer(2), firstUpdate.getProjectView().getProjectTier());
        assertEquals("original", firstUpdate.getProjectView().getDescription());
        assertEquals("panda bear", firstUpdate.getProjectVersionView().getNickname());
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, firstUpdate.getProjectVersionView().getPhase());

        ProjectSyncModel update2ProjectSyncModel = new ProjectSyncModel("testSync", "testSyncVersion");
        update2ProjectSyncModel.setProjectTier(3);
        update2ProjectSyncModel.setNickname("honey badger");
        ProjectVersionWrapper secondUpdate = ProjectServiceTestIT.projectService.syncProjectAndVersion(update2ProjectSyncModel, true);
        assertEquals(new Integer(3), secondUpdate.getProjectView().getProjectTier());
        assertEquals("original", secondUpdate.getProjectView().getDescription());
        assertEquals("honey badger", secondUpdate.getProjectVersionView().getNickname());
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, secondUpdate.getProjectVersionView().getPhase());
    }

}
