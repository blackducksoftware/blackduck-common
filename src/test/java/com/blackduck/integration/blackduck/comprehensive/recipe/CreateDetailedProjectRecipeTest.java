package com.blackduck.integration.blackduck.comprehensive.recipe;

import com.blackduck.integration.blackduck.TimingExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class CreateDetailedProjectRecipeTest extends BasicRecipe {
    private ProjectView projectView;

    @AfterEach
    public void cleanup() throws IntegrationException {
        deleteProject(projectView);
    }

    @Test
    public void testCreatingAProject() throws IntegrationException {
        /*
         * let's post the project/version in Black Duck
         */
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, PROJECT_VERSION_NAME);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectSyncModel.createProjectRequest());
        HttpUrl projectUrl = projectVersionWrapper.getProjectView().getHref();

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the BlackDuckService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        projectView = blackDuckApiClient.getResponse(projectUrl, ProjectView.class);
        ProjectVersionView projectVersionView = blackDuckApiClient.getResponse(projectView.metaCanonicalVersionLink());

        Assertions.assertEquals(uniqueProjectName, projectView.getName());
        Assertions.assertEquals("A sample testing project to demonstrate blackduck-common capabilities.", projectView.getDescription());

        Assertions.assertEquals(PROJECT_VERSION_NAME, projectVersionView.getVersionName());
        Assertions.assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.getPhase());
        Assertions.assertEquals(ProjectVersionDistributionType.OPENSOURCE, projectVersionView.getDistribution());
    }

}
