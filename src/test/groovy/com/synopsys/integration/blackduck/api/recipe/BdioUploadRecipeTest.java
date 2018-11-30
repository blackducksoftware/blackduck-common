package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

@Tag("integration")
public class BdioUploadRecipeTest extends BasicRecipe {
    private final String codeLocationName = "hub_common_27_0_0_SNAPSHOT_upload_recipe";
    private final String uniqueProjectName = "hub-common_with_project_in_bdio";
    private Optional<ProjectVersionWrapper> projectVersionWrapper;

    @AfterEach
    public void cleanup() {
        if (projectVersionWrapper.isPresent()) {
            deleteProject(projectVersionWrapper.get().getProjectView());
        }
        deleteCodeLocation(codeLocationName);
    }

    @Test
    public void testBdioUpload() throws IntegrationException, InterruptedException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_with_project_section.jsonld");
        /**
         * in this case we can upload the bdio and it will be mapped to a project and version because it has the Project information within the bdio file
         */
        final IntLogger logger = new BufferedIntLogger();
        final UploadRunner uploadRunner = new UploadRunner(logger, blackDuckService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, file));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, "27.0.0-SNAPSHOT");
        assertTrue(projectVersionWrapper.isPresent());
        final List<CodeLocationView> versionCodeLocations = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        assertEquals(1, versionCodeLocations.size());
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.getName());
    }

    @Test
    public void testBdioUploadAndMapToVersion() throws InterruptedException, IntegrationException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_without_project_section.jsonld");
        /**
         * in this case we upload the bdio but we have to map it to a project and version ourselves since the Project information is missing in the bdio file
         */
        final IntLogger logger = new BufferedIntLogger();

        final UploadRunner uploadRunner = new UploadRunner(logger, blackDuckService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, file));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        codeLocationCreationService.createCodeLocations(scanRequest);

        /**
         * now that the file is uploaded, we want to lookup the code location that was created by the upload. in this case we know the name of the code location that was specified in the bdio file
         */
        Optional<CodeLocationView> optionalCodeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        final int maxAttempts = 6;
        int attempt = 0;
        while (!optionalCodeLocationView.isPresent() && attempt < maxAttempts) {
            // creating the code location can take a few seconds
            attempt++;
            Thread.sleep(5000);
            optionalCodeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        }
        final CodeLocationView codeLocationView = optionalCodeLocationView.get();
        System.out.println(codeLocationView.getHref().get());

        /**
         * then we map the code location to a version
         */
        final String versionName = "27.0.0-SNAPSHOT";
        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder(uniqueProjectName, versionName);
        projectService.createProject(projectRequestBuilder.build());
        projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, versionName);

        final NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();

        codeLocationService.mapCodeLocation(codeLocationView, projectVersionWrapper.get().getProjectVersionView());

        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, new HashSet<>(Arrays.asList(codeLocationView.getName())), 15 * 60);

        final List<CodeLocationView> versionCodeLocations = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.getName());
    }

}
