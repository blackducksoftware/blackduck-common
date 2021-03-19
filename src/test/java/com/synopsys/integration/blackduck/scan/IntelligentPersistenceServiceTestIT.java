package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
public class IntelligentPersistenceServiceTestIT {
    private static final String PROJECT_NAME = "jackson-core";
    private static final String PROJECT_VERSION = "3.0.0-SNAPSHOT";
    private static final String CODE_LOCATION_NAME = "jackson-core-master/jackson-core/com.fasterxml.jackson.core/jackson-core/3.0.0-SNAPSHOT maven/bom";
    //TODO Uncomment when BlackDuck officially supports intelligent persistence mode.

    //    @Test
    //    public void testScan() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/scans/small_scan_test.bdio").getFile());
    //        scanService.performScan(bdioFile);
    //    }
    //
    //    @Test
    //    public void testScanBatch() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/scans/small_scan_test.bdio").getFile());
    //        NameVersion projectNameVersion = new NameVersion(PROJECT_NAME, PROJECT_VERSION);
    //        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, CODE_LOCATION_NAME, bdioFile);
    //        scanService.performScan(new UploadBatch(uploadTarget));
    //    }
    //
    //    @Test
    //    public void testScanAndWait() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        CodeLocationService codeLocationService = blackDuckServicesFactory.createCodeLocationService();
    //
    //        IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/scans/small_scan_test.bdio").getFile());
    //        NameVersion projectNameVersion = new NameVersion(PROJECT_NAME, PROJECT_VERSION);
    //        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, CODE_LOCATION_NAME, bdioFile);
    //        scanService.performScanAndWait(new UploadBatch(uploadTarget), 300L);
    //        Optional<CodeLocationView> optionalCodeLocationView = codeLocationService.getCodeLocationByName(CODE_LOCATION_NAME);
    //
    //        assertTrue(optionalCodeLocationView.isPresent());
    //    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
            File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanMissingHeader.bdio").getFile());
            NameVersion projectNameVersion = new NameVersion(PROJECT_NAME, PROJECT_VERSION);
            UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, CODE_LOCATION_NAME, bdioFile);
            scanService.performScan(uploadTarget);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }
}
