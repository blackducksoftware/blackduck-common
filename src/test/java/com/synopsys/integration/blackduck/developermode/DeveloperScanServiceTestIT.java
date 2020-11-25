package com.synopsys.integration.blackduck.developermode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
public class DeveloperScanServiceTestIT {

    @Test
    public void testFileDirectory() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            DeveloperScanService developerScanService = blackDuckServicesFactory.createDeveloperScanService();
            File bdioFile = new File(getClass().getResource("/bdio/developer_scan/").getFile());
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            developerScanService.performDeveloperScan(bdioFile, timeout);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testFileMissing() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            DeveloperScanService developerScanService = blackDuckServicesFactory.createDeveloperScanService();
            File bdioFile = new File("/bdio/developer_scan/badPath.bdio");
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            developerScanService.performDeveloperScan(bdioFile, timeout);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testExtensionInvalid() throws Exception {
        File bdioFile = Files.createTempFile("badExtension", "txt").toFile();
        bdioFile.deleteOnExit();
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            DeveloperScanService developerScanService = blackDuckServicesFactory.createDeveloperScanService();
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            developerScanService.performDeveloperScan(bdioFile, timeout);
            fail();
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
            DeveloperScanService developerScanService = blackDuckServicesFactory.createDeveloperScanService();
            File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanMissingHeader.bdio").getFile());
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            developerScanService.performDeveloperScan(bdioFile, timeout);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }

    @Test
    public void testDeveloperScan() throws Exception {
        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        DeveloperScanService developerScanService = blackDuckServicesFactory.createDeveloperScanService();
        File bdioFile = new File(getClass().getResource("/bdio/developer_scan/developerScanTest.bdio").getFile());
        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
        List<BomMatchDeveloperView> results = developerScanService.performDeveloperScan(bdioFile, timeout);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
