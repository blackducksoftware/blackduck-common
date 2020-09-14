package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Date;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.comprehensive.BlackDuckServices;
import com.synopsys.integration.blackduck.comprehensive.VerifyNotifications;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
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
        BinaryScanData binaryScanData = createLocalBinaryScanData(blackDuckServices);

        BinaryScanUploadService binaryScanUploadService = blackDuckServices.blackDuckServicesFactory.createBinaryScanUploadService();
        BinaryScanBatchOutput binaryScanBatchOutput = binaryScanUploadService.uploadBinaryScanAndWait(binaryScanData.binaryScan, 15 * 60);
        for (BinaryScanOutput uploadOutput : binaryScanBatchOutput) {
            assertEquals(Result.SUCCESS, uploadOutput.getResult());
        }

        VerifyNotifications.verify(binaryScanData.userView, blackDuckServices.notificationService, binaryScanData.userStartDate, binaryScanData.systemStartDate);
    }

    @Test
    @Disabled
    //disabled because special config is needed to support /api/uploads (binary scan)
    public void testCodeLocationFromBinaryScanUploadWhenNotConfigured() throws Exception {
        BlackDuckServices blackDuckServices = new BlackDuckServices(intHttpClientTestHelper);
        BinaryScanData binaryScanData = createLocalBinaryScanData(blackDuckServices);

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

    private BinaryScanData createLocalBinaryScanData(BlackDuckServices blackDuckServices) throws IntegrationException {
        String projectName = "binary_scan_project";
        String projectVersionName = "0.0.1";
        String codeLocationName = "binary scan test code location";

        UserView currentUser = blackDuckServices.blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        Date userStartDate = blackDuckServices.notificationService.getLatestUserNotificationDate(currentUser);
        Date systemStartDate = blackDuckServices.notificationService.getLatestNotificationDate();

        File file = new File("/Users/ekerwin/Downloads/integration-bdio-21.0.2-sources.jar");
        BinaryScan binaryScan = new BinaryScan(file, projectName, projectVersionName, codeLocationName);

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
