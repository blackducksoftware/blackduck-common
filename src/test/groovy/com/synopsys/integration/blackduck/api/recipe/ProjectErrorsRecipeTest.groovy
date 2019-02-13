package com.synopsys.integration.blackduck.api.recipe

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.generated.view.ProjectView
import com.synopsys.integration.blackduck.exception.BlackDuckApiException
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
@ExtendWith(TimingExtension.class)
class ProjectErrorsRecipeTest extends BasicRecipe {
    private static final String PROJECT_NAME_NOT_FOUND = 'Project Name That Should Never Exist'

    private final String uniqueName = PROJECT_NAME_NOT_FOUND + System.currentTimeMillis()
    private ProjectView projectView

    @AfterEach
    void cleanup() {
        deleteProject(projectView)
    }

    @Test
    void testTryingToFindProjectThatDoesNotExist() {
        /*
         * Let's try and find a project that doesn't exist, which should return Optional.empty()
         */
        Optional<ProjectView> projectView = projectService.getProjectByName(uniqueName)
        assertFalse(projectView.isPresent())
    }

    @Test
    void testTryingToCreateAProjectThatAlreadyExists() {
        /*
         * First, create a project with a unique name
         */
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueName, PROJECT_VERSION_NAME)
        ProjectVersionWrapper projectVersionWrapper = projectService.createProject(projectSyncModel.createProjectRequest())
        projectView = projectVersionWrapper.projectView;

        /*
         * Try to create a project with the same name, which should throw an Exception
         */
        try {
            projectService.createProject(projectSyncModel.createProjectRequest())
            fail('Should have thrown an IntegrationRestException')
        } catch (Exception e) {
            assertTrue(e instanceof BlackDuckApiException)
            //since the project already existed, a 412 Precondition Failed http error response should occur
            assertEquals(412, ((BlackDuckApiException) e).getOriginalIntegrationRestException().getHttpStatusCode())
        }
    }

}
