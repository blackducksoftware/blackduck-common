package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComponentManagementRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;

    @BeforeEach
    public void setup() throws Exception {
        String uniqueProjectName = BasicRecipe.PROJECT_NAME + System.currentTimeMillis();
        String versionName = BasicRecipe.PROJECT_VERSION_NAME;

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
    public void cleanup() {
        if (projectVersionWrapper != null) {
            deleteProject(projectVersionWrapper.getProjectView().getName());
        }
    }

    @Test
    public void testAddingAComponent() throws Exception {
        ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.group = "commons-fileupload";
        externalId.name = "commons-fileupload";
        externalId.version = "1.2.1";

        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        List<ProjectVersionComponentView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        ProjectVersionComponentView component = bomComponents.get(0);
        assertEquals("Apache Commons FileUpload", component.getComponentName());
        assertEquals("1.2.1", component.getComponentVersionName());
    }

}
