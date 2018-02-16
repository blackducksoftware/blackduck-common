package com.blackducksoftware.integration.hub.api.recipe;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.service.CodeLocationService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;

public class BdioUploadRecipeTest extends BasicRecipe {
    private final String codeLocationName = "hub_common_27_0_0_SNAPSHOT_upload_recipe";
    private final String uniqueProjectName = "hub-common_with_project_in_bdio";
    private ProjectVersionWrapper projectVersionWrapper;

    @After
    public void cleanup() {
        if (projectVersionWrapper != null) {
            deleteProject(projectVersionWrapper.getProjectView().name);
        }
        deleteCodeLocation(codeLocationName);
    }

    @Test
    public void testBdioUpload() throws IntegrationException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_with_project_section.jsonld");
        /**
         * in this case we can upload the bdio and it will be mapped to a project and version because it has the Project information within the bdio file
         */
        hubServicesFactory.createCodeLocationService().importBomFile(file);

        final ProjectService projectService = hubServicesFactory.createProjectService();
        projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, "27.0.0-SNAPSHOT");
        final HubService hubService = hubServicesFactory.createHubService();
        final List<CodeLocationView> versionCodeLocations = hubService.getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        assertEquals(1, versionCodeLocations.size());
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.name);
    }

    @Test
    public void testBdioUploadAndMapToVersion() throws IntegrationException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_without_project_section.jsonld");
        /**
         * in this case we upload the bdio but we have to map it to a project and version ourselves since the Project information is missing in the bdio file
         */
        hubServicesFactory.createCodeLocationService().importBomFile(file);

        /**
         * now that the file is uploaded, we want to lookup the code location that was created by the upload. in this case we know the name of the code location that was specified in the bdio file
         */
        final CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService();
        final CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);

        /**
         * then we map the code location to a version
         */
        final ProjectService projectService = hubServicesFactory.createProjectService();
        projectVersionWrapper = projectService.getProjectVersionAndCreateIfNeeded(uniqueProjectName, "27.0.0-SNAPSHOT");

        codeLocationService.mapCodeLocation(codeLocationView, projectVersionWrapper.getProjectVersionView());

        final HubService hubService = hubServicesFactory.createHubService();
        final List<CodeLocationView> versionCodeLocations = hubService.getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.name);
    }
}
