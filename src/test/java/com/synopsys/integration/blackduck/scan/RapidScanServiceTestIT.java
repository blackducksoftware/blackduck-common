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
public class RapidScanServiceTestIT {

    //TODO Uncomment when BlackDuck officially supports developer mode.
    //    @Test
    //    public void testScan() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
    //        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
    //        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(bdioFile, timeout, 5);
    //        assertNotNull(results);
    //        assertFalse(results.isEmpty());
    //    }
    //
    //    @Test
    //    public void testScanBatch() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
    //        NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
    //        String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
    //        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
    //        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
    //        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(new UploadBatch(uploadTarget), timeout, 5);
    //        assertNotNull(results);
    //        assertFalse(results.isEmpty());
    //    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
            File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanMissingHeader.bdio").getFile());
            NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
            String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
            UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            rapidScanService.performScan(uploadTarget, timeout);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }
}
