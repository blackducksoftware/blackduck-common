package com.blackduck.integration.blackduck.comprehensive.recipe;

import com.blackduck.integration.bdio.model.Forge;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.service.model.ProjectSyncModel;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComponentManagementRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;

    @BeforeEach
    public void setup() throws Exception {
        String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        String versionName = PROJECT_VERSION_NAME;

        /**
         * we can get the project and version like this, and if they don't exist we will create them
         */
        Optional<ProjectView> projectView = projectService.getProjectByName(uniqueProjectName);
        ProjectSyncModel projectSyncModel = createProjectSyncModel(uniqueProjectName, versionName);
        if (!projectView.isPresent()) {
            projectService.createProject(projectSyncModel.createProjectRequest());
            projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, versionName).get();
        } else {
            // the project exists, check the version
            Optional<ProjectVersionView> projectVersionView = projectService.getProjectVersion(projectView.get(), versionName);
            if (projectVersionView.isPresent()) {
                projectVersionWrapper = new ProjectVersionWrapper(projectView.get(), projectVersionView.get());
            } else {
                projectService.createProjectVersion(projectView.get(), projectSyncModel.createProjectVersionRequest());
                projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, versionName).get();
            }
        }
    }

    @AfterEach
    public void cleanup() throws IntegrationException {
        if (projectVersionWrapper != null) {
            deleteProject(projectVersionWrapper.getProjectView().getName());
        }
    }

    @Test
    public void testAddingAComponent() throws Exception {
        ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.setGroup("commons-fileupload");
        externalId.setName("commons-fileupload");
        externalId.setVersion("1.2.1");

        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        List<ProjectVersionComponentVersionView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        ProjectVersionComponentVersionView component = bomComponents.get(0);
        assertEquals("Apache Commons FileUpload", component.getComponentName());
        assertEquals("1.2.1", component.getComponentVersionName());
    }

}
