package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.hub.bdio.model.Forge;
import com.synopsys.integration.hub.bdio.model.externalid.ExternalId;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
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
