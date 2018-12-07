package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Tag("integration")
public class ComponentManagementRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;

    @BeforeEach
    public void setup() throws Exception {
        final String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        final String versionName = PROJECT_VERSION_NAME;

        /**
         * we can get the project and version like this, and if they don't exist we will post them
         */
        final Optional<ProjectView> projectView = projectService.getProjectByName(uniqueProjectName);
        final ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, versionName);
        if (!projectView.isPresent()) {
            projectService.createProject(projectRequest);
            projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, versionName).get();
        } else {
            // the project exists, check the version
            final Optional<ProjectVersionView> projectVersionView = projectService.getProjectVersion(projectView.get(), versionName);
            if (projectVersionView.isPresent()) {
                projectVersionWrapper = new ProjectVersionWrapper(projectView.get(), projectVersionView.get());
            } else {
                projectService.createProjectVersion(projectView.get(), projectRequest.getVersionRequest());
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
        final ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.group = "commons-fileupload";
        externalId.name = "commons-fileupload";
        externalId.version = "1.2.1";

        projectService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        final List<VersionBomComponentView> bomComponents = projectService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        final VersionBomComponentView component = bomComponents.get(0);
        assertEquals("Apache Commons FileUpload", component.getComponentName());
        assertEquals("1.2.1", component.getComponentVersionName());
    }

}
