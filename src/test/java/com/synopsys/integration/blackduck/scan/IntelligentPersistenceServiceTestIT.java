package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
public class IntelligentPersistenceServiceTestIT {
    //TODO Uncomment when BlackDuck officially supports developer mode.
    //    @Test
    //    public void testScan() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanTest.bdio").getFile());
    //        scanService.performScan(bdioFile);
    //    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            IntelligentPersistenceScanService scanService = blackDuckServicesFactory.createIntelligentPersistenceScanService();
            File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanMissingHeader.bdio").getFile());
            scanService.performScan(bdioFile);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }
}
