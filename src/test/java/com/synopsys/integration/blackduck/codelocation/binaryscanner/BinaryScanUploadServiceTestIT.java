package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import static com.synopsys.integration.blackduck.service.dataservice.ProjectServiceTestIT.JAPANESE_CODE_LOCATION_NAME;
import static com.synopsys.integration.blackduck.service.dataservice.ProjectServiceTestIT.JAPANESE_PROJECT_NAME;
import static com.synopsys.integration.blackduck.service.dataservice.ProjectServiceTestIT.JAPANESE_VERSION_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.comprehensive.BlackDuckServices;
import com.synopsys.integration.blackduck.comprehensive.VerifyNotifications;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BinaryScanUploadServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    @Disabled
    //disabled because special config is needed to support /api/uploads (binary scan)
    public void testCodeLocationFromBinaryScanUpload() throws Exception {
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
        BinaryScanData binaryScanData = createBinaryScanData(blackDuckServices, createTestBinaryScan());

        assertBinaryUploadCompleted(blackDuckServices, binaryScanData);
    }

    @Test
    public void testCodeLocationFromBinaryScanUploadWhenNotConfigured() throws Exception {
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
        BinaryScanData binaryScanData = createBinaryScanData(blackDuckServices, createTestBinaryScan());

        BinaryScanUploadService binaryScanUploadService = blackDuckServices.blackDuckServicesFactory.createBinaryScanUploadService();
        BinaryScanBatchOutput binaryScanBatchOutput = binaryScanUploadService.uploadBinaryScanAndWait(binaryScanData.binaryScan, 15 * 60);
        BufferedIntLogger logger = new BufferedIntLogger();
        try {
            binaryScanBatchOutput.throwExceptionForError(logger);
        } catch (Exception e) {
            assertTrue(e instanceof BlackDuckIntegrationException);
            assertTrue(e.getMessage().startsWith("Error when uploading binary scan"));
            assertTrue(logger.getOutputString(LogLevel.ERROR).contains(e.getMessage()));
        }
    }

    @Test
    @Disabled
    //disabled because special config is needed to support /api/uploads (binary scan)
    //also, because as of 2020.8.0, binary upload doesn't support utf-8
    public void testJapaneseCharactersForBinaryUpload() throws IntegrationException, InterruptedException {
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);

        File binaryFile = getTestBinaryFile();
        BinaryScan binaryScan = new BinaryScan(binaryFile, JAPANESE_PROJECT_NAME, JAPANESE_VERSION_NAME, JAPANESE_CODE_LOCATION_NAME);
        BinaryScanData binaryScanData = createBinaryScanData(blackDuckServices, binaryScan);

        assertBinaryUploadCompleted(blackDuckServices, binaryScanData);

        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = blackDuckServices.projectService.getProjectVersion(JAPANESE_PROJECT_NAME, JAPANESE_VERSION_NAME);
        Optional<CodeLocationView> optionalCodeLocationView = blackDuckServices.codeLocationService.getCodeLocationByName(JAPANESE_CODE_LOCATION_NAME);

        assertTrue(optionalProjectVersionWrapper.isPresent());
        assertTrue(optionalCodeLocationView.isPresent());
    }

    private void assertBinaryUploadCompleted(BlackDuckServices blackDuckServices, BinaryScanData binaryScanData) throws InterruptedException, IntegrationException {
        BinaryScanUploadService binaryScanUploadService = blackDuckServices.blackDuckServicesFactory.createBinaryScanUploadService();
        BinaryScanBatchOutput binaryScanBatchOutput = binaryScanUploadService.uploadBinaryScanAndWait(binaryScanData.binaryScan, 15 * 60);
        for (BinaryScanOutput uploadOutput : binaryScanBatchOutput) {
            assertEquals(Result.SUCCESS, uploadOutput.getResult());
        }

        VerifyNotifications.verify(binaryScanData.userView, blackDuckServices.notificationService, binaryScanData.userStartDate, binaryScanData.systemStartDate);
    }

    private BinaryScan createTestBinaryScan() {
        String projectName = "binary_scan_project";
        String projectVersionName = "0.0.1";
        String codeLocationName = "binary scan test code location";

        File binaryFile = getTestBinaryFile();
        return new BinaryScan(binaryFile, projectName, projectVersionName, codeLocationName);
    }

    @NotNull
    private File getTestBinaryFile() {
        return new File(getClass().getResource("/integration-bdio-21.0.2-sources.jar").getFile());
    }

    private BinaryScanData createBinaryScanData(BlackDuckServices blackDuckServices, BinaryScan binaryScan) throws IntegrationException {
        UserView currentUser = blackDuckServices.blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        Date userStartDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        Date systemStartDate = blackDuckServices.notificationService.getLatestNotificationDate();

        return new BinaryScanData(currentUser, userStartDate, systemStartDate, binaryScan);
    }

    private static class BinaryScanData {
        public UserView userView;
        public Date userStartDate;
        public Date systemStartDate;
        public BinaryScan binaryScan;

        public BinaryScanData(UserView userView, Date userStartDate, Date systemStartDate, BinaryScan binaryScan) {
            this.userView = userView;
            this.userStartDate = userStartDate;
            this.systemStartDate = systemStartDate;
            this.binaryScan = binaryScan;
        }
    }

}
