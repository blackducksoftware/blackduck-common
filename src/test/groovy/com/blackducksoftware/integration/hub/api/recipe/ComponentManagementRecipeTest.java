package com.blackducksoftware.integration.hub.api.recipe;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class ComponentManagementRecipeTest extends BasicRecipe {
    private ProjectVersionWrapper projectVersionWrapper;

    @Before
    public void setup() throws Exception {
        final String uniqueProjectName = PROJECT_NAME + System.currentTimeMillis();
        final ProjectRequest projectRequest = createProjectRequest(uniqueProjectName, PROJECT_VERSION_NAME);
        final ProjectService projectService = hubServicesFactory.createProjectService();

        /**
         * we can get the project and version like this, and if they don't exist they will be created for us
         */
        projectVersionWrapper = projectService.getProjectVersionAndCreateIfNeeded(projectRequest);
    }

    @After
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
