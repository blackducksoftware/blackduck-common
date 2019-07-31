package com.synopsys.integration.blackduck.comprehensive;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectBomService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.log.IntLogger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectBomServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testAddingComponentToBom() throws Exception {
        String projectName = "adding_component_test";
        String projectVersionName = "1.0.0";

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the project, if it exists
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckService, projectName);

        // create the project
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        // verify the bom
        List<VersionBomComponentView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        ExternalId externalId = new ExternalIdFactory().createMavenExternalId("com.blackducksoftware.integration", "blackduck-common", "43.0.0");
        projectBomService.addComponentToProjectVersion(externalId, projectVersionWrapper.getProjectVersionView());

        bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

    @Test
    public void testAddingProjectVersionToBom() throws Exception {
        String projectName = "adding_project_version_test";
        String projectVersionName = "1.0.0";

        String projectNameToAdd = "to_add_project";
        String projectVersionNameToAdd = "to_add_project_version";

        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        IntLogger logger = blackDuckServicesFactory.getLogger();

        // delete the projects, if they exist
        intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckService, projectName);

        // as this might have been previously added to a bom, it might still register as in use - try a few times
        int count = 0;
        boolean succeeded = false;
        while (count < 10 && !succeeded) {
            try {
                intHttpClientTestHelper.deleteIfProjectExists(logger, projectService, blackDuckService, projectNameToAdd);
                succeeded = true;
            } catch (Exception ignored) {
                // ignored
            }
            count++;
        }

        // create the projects
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectName, projectVersionName);
        ProjectVersionWrapper projectVersionWrapper = projectService.syncProjectAndVersion(projectSyncModel);

        ProjectSyncModel projectSyncModelToAdd = ProjectSyncModel.createWithDefaults(projectNameToAdd, projectVersionNameToAdd);
        ProjectVersionWrapper projectVersionWrapperToAdd = projectService.syncProjectAndVersion(projectSyncModelToAdd);

        // verify the boms
        List<VersionBomComponentView> bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(0, bomComponents.size());

        projectBomService.addProjectVersionToProjectVersion(projectVersionWrapperToAdd.getProjectVersionView(), projectVersionWrapper.getProjectVersionView());

        bomComponents = projectBomService.getComponentsForProjectVersion(projectVersionWrapper.getProjectVersionView());
        assertEquals(1, bomComponents.size());
    }

}
