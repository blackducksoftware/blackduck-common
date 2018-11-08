package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;

@Tag("integration")
public class ComponentManagementRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;

    @BeforeEach
    public void setup() throws Exception {
        final String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        final ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, PROJECT_VERSION_NAME);
        final ProjectService projectService = hubServicesFactory.createProjectService();

        /**
         * we can get the project and version like this, and if they don't exist they will be created for us
         */
        projectVersionWrapper = projectService.syncProjectAndVersion(projectRequest, false);
    }

    @AfterEach
    public void cleanup() {
        if (projectVersionWrapper != null) {
            deleteProject(projectVersionWrapper.getProjectView().name);
        }
    }

    @Test
    public void testAddingAComponent() throws Exception {
        final ProjectService projectService = hubServicesFactory.createProjectService();

        final ExternalId externalId = new ExternalId(Forge.MAVEN);
        externalId.group = "commons-fileupload";
        externalId.name = "commons-fileupload";
        externalId.version = "1.2.1";

        projectService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        final List<VersionBomComponentView> bomComponents = projectService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        final VersionBomComponentView component = bomComponents.get(0);
        assertEquals("Apache Commons FileUpload", component.componentName);
        assertEquals("1.2.1", component.componentVersionName);
    }

}
