package com.synopsys.integration.blackduck.api.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.BdioUploadCodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadRunner;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntBufferedLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class BdioUploadRecipeTest extends BasicRecipe {
    private final String codeLocationName = "hub_common_27_0_0_SNAPSHOT_upload_recipe";
    private final String uniqueProjectName = "hub-common_with_project_in_bdio";
    private Optional<ProjectVersionWrapper> projectVersionWrapper;

    @After
    public void cleanup() {
        if (projectVersionWrapper.isPresent()) {
            deleteProject(projectVersionWrapper.get().getProjectView().name);
        }
        deleteCodeLocation(codeLocationName);
    }

    @Test
    public void testBdioUpload() throws IntegrationException, InterruptedException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_with_project_section.jsonld");
        /**
         * in this case we can upload the bdio and it will be mapped to a project and version because it has the Project information within the bdio file
         */
        final IntLogger logger = new IntBufferedLogger();
        final HubService hubService = hubServicesFactory.createHubService();
        final CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService();
        final NotificationService notificationService = hubServicesFactory.createNotificationService();

        final UploadRunner uploadRunner = new UploadRunner(logger, hubService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, file));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        final CodeLocationCreationService codeLocationCreationService = new CodeLocationCreationService(hubService, logger, jsonFieldResolver, codeLocationService, notificationService);
        codeLocationCreationService.createCodeLocationsAndWait(scanRequest, 15 * 60);

        final ProjectService projectService = hubServicesFactory.createProjectService();
        projectVersionWrapper = projectService.getProjectVersion(uniqueProjectName, "27.0.0-SNAPSHOT");
        assertTrue(projectVersionWrapper.isPresent());
        final List<CodeLocationView> versionCodeLocations = hubService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        assertEquals(1, versionCodeLocations.size());
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.name);
    }

    @Test
    public void testBdioUploadAndMapToVersion() throws InterruptedException, IntegrationException {
        final File file = restConnectionTestHelper.getFile("bdio/hub_common_bdio_without_project_section.jsonld");
        /**
         * in this case we upload the bdio but we have to map it to a project and version ourselves since the Project information is missing in the bdio file
         */
        final IntLogger logger = new IntBufferedLogger();
        final HubService hubService = hubServicesFactory.createHubService();
        final CodeLocationService codeLocationService = hubServicesFactory.createCodeLocationService();
        final NotificationService notificationService = hubServicesFactory.createNotificationService();

        final UploadRunner uploadRunner = new UploadRunner(logger, hubService);
        final UploadBatch uploadBatch = new UploadBatch();
        uploadBatch.addUploadTarget(UploadTarget.createDefault(codeLocationName, file));
        final BdioUploadCodeLocationCreationRequest scanRequest = new BdioUploadCodeLocationCreationRequest(uploadRunner, uploadBatch);

        final CodeLocationCreationService codeLocationCreationService = new CodeLocationCreationService(hubService, logger, jsonFieldResolver, codeLocationService, notificationService);
        codeLocationCreationService.createCodeLocations(scanRequest);

        /**
         * now that the file is uploaded, we want to lookup the code location that was created by the upload. in this case we know the name of the code location that was specified in the bdio file
         */
        final CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
        System.out.println(codeLocationView._meta.href);

        /**
         * then we map the code location to a version
         */
        final ProjectService projectService = hubServicesFactory.createProjectService();
        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder(uniqueProjectName, "27.0.0-SNAPSHOT");

        projectVersionWrapper = Optional.of(projectService.syncProjectAndVersion(projectRequestBuilder.build(), false));

        final NotificationTaskRange notificationTaskRange = codeLocationCreationService.calculateCodeLocationRange();

        codeLocationService.mapCodeLocation(codeLocationView, projectVersionWrapper.get().getProjectVersionView());

        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, new HashSet<>(Arrays.asList(codeLocationView.name)), 15 * 60);

        final List<CodeLocationView> versionCodeLocations = hubService.getAllResponses(projectVersionWrapper.get().getProjectVersionView(), ProjectVersionView.CODELOCATIONS_LINK_RESPONSE);
        final CodeLocationView versionCodeLocation = versionCodeLocations.get(0);
        assertEquals(codeLocationName, versionCodeLocation.name);
    }

}
