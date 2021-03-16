package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
public class RapidScanServiceTestIT {

    //TODO Uncomment when BlackDuck officially supports developer mode.
    //    @Test
    //    public void testScan() throws Exception {
    //        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    //        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    //        RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
    //        File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanTest.bdio").getFile());
    //        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
    //        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(bdioFile, timeout, 5);
    //        assertNotNull(results);
    //        assertFalse(results.isEmpty());
    //    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
            File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanMissingHeader.bdio").getFile());
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            rapidScanService.performScan(bdioFile, timeout);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }
}
