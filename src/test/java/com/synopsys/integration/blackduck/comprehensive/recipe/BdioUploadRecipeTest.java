package com.synopsys.integration.blackduck.comprehensive.recipe;

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
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaitResult;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BdioUploadRecipeTest extends BasicRecipe {
    private final String codeLocationName = "hub_common_27_0_0_SNAPSHOT_upload_recipe";
    private final String projectName = "hub-common_with_project_in_bdio";
    private final String versionName = "27.0.0-SNAPSHOT";
    private final NameVersion projectAndVersion = new NameVersion(projectName, versionName);
    private Optional<ProjectVersionWrapper> projectVersionWrapper;

    @AfterEach
    public void cleanup() throws IntegrationException {
        if (projectVersionWrapper.isPresent()) {
            deleteProject(projectVersionWrapper.get().getProjectView());
        }
        deleteCodeLocation(codeLocationName);
    }

    @Test
    public void testBdioUpload() throws IntegrationException, InterruptedException {
        File file = BasicRecipe.restConnectionTestHelper.getFile("bdio/hub_common_bdio_with_project_section.jsonld");

        //in this case we can upload the bdio and it will be mapped to a project and version because it has the Project information within the bdio file
        IntLogger logger = new BufferedIntLogger();
        UploadBatchRunner uploadBatchRunner = new UploadBatchRunner(logger, blackDuckService, requestFactory, BlackDuckServicesFactory.NO_THREAD_EXECUTOR_SERVICE);
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(projectAndVersion, codeLocationName, file));
        BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        projectVersionWrapper = projectService.getProjectVersion(projectAndVersion);
        assertTrue(projectVersionWrapper.isPresent());
        List<CodeLocationView> versionCodeLocations = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        assertEquals(1, versionCodeLocations.size());
        CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.getName());
    }

    @Test
    public void testBdioUploadAndMapToVersion() throws InterruptedException, IntegrationException {
        File file = BasicRecipe.restConnectionTestHelper.getFile("bdio/hub_common_bdio_without_project_section.jsonld");
        // in this case we upload the bdio but we have to map it to a project and version ourselves since the Project information is missing in the bdio file
        IntLogger logger = new BufferedIntLogger();

        UploadBatchRunner uploadBatchRunner = new UploadBatchRunner(logger, blackDuckService, requestFactory, BlackDuckServicesFactory.NO_THREAD_EXECUTOR_SERVICE);
        UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(projectAndVersion, codeLocationName, file));
        BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadBatchRunner, uploadBatch);

        codeLocationCreationService.createCodeLocations(scanRequest);

        // now that the file is uploaded, we want to lookup the code location that was created by the upload. in this case we know the name of the code location that was specified in the bdio file
        Optional<CodeLocationView> optionalCodeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        int maxAttempts = 6;
        int attempt = 0;
        while (!optionalCodeLocationView.isPresent() && attempt < maxAttempts) {
            // creating the code location can take a few seconds
            attempt++;
            Thread.sleep(5000);
            optionalCodeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        }
        CodeLocationView codeLocationView = optionalCodeLocationView.get();

        // then we map the code location to a version
        String versionName = "27.0.0-SNAPSHOT";
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectAndVersion);
        projectService.createProject(projectSyncModel.createProjectRequest());
        projectVersionWrapper = projectService.getProjectVersion(projectAndVersion);
        List<CodeLocationView> versionCodeLocations = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        assertTrue(versionCodeLocations.isEmpty());

        NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();
        System.out.println(RestConstants.formatDate(notificationTaskRange.getStartDate()));
        System.out.println(RestConstants.formatDate(notificationTaskRange.getEndDate()));

        codeLocationService.mapCodeLocation(codeLocationView, projectVersionWrapper.get().getProjectVersionView());

        CodeLocationWaitResult waitResult = codeLocationCreationService.waitForCodeLocations(notificationTaskRange, projectAndVersion, new HashSet<>(Arrays.asList(codeLocationView.getName(), "pants")), 1, 3 * 60);
        System.out.println("wait status: " + waitResult.getStatus());
        if (waitResult.getErrorMessage().isPresent()) {
            System.out.println(waitResult.getErrorMessage().get());
        }
        waitResult.getCodeLocationNames().stream().forEach(System.out::println);

        versionCodeLocations = blackDuckService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.getName());
    }

}
