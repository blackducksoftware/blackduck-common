package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.enumeration.ProjectVersionPhaseType
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.service.BlackDuckService
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.assertEquals

@Tag("integration")
@ExtendWith(TimingExtension.class)
class CreateDetailedProjectRecipeTest extends BasicRecipe {
    private ProjectView projectView

    @AfterEach
    void cleanup() {
        deleteProject(projectView)
    }

    @Test
    void testCreatingAProject() {
        /*
         * let's post the project/version in Black Duck
         */
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, PROJECT_VERSION_NAME)
        ProjectService projectService = blackDuckServicesFactory.createProjectService()
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectSyncModel.createProjectRequest())
        String projectUrl = projectVersionWrapper.projectView.getHref().get()

        /*
         * using the url of the created project, we can now verify that the
         * fields are set correctly with the BlackDuckService, a general purpose API
         * wrapper to handle common GET requests and their response payloads
         */
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService()
        projectView = blackDuckService.getResponse(projectUrl, ProjectView.class)
        ProjectVersionView projectVersionView = blackDuckService.getResponse(projectView, ProjectView.CANONICALVERSION_LINK_RESPONSE).get()

        assertEquals(uniqueProjectName, projectView.name)
        assertEquals('A sample testing project to demonstrate blackduck-common capabilities.', projectView.description)

        assertEquals(PROJECT_VERSION_NAME, projectVersionView.versionName)
        assertEquals(ProjectVersionPhaseType.DEVELOPMENT, projectVersionView.phase)
        assertEquals(LicenseFamilyLicenseFamilyRiskRulesReleaseDistributionType.OPENSOURCE, projectVersionView.distribution)
    }

}
