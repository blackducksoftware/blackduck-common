package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.VersionSupport;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
public class RapidScanServiceTestIT {
    @Test
    public void testScan() throws Exception {
        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();

        checkVersionForRapidScan(blackDuckServicesFactory);

        RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
        String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(uploadTarget, timeout, 5);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getViolatingPolicyNames());
    }

    private void checkVersionForRapidScan(BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
        String version = blackDuckRegistrationService.getBlackDuckServerData().getVersion();
        Assumptions.assumeTrue(VersionSupport.isVersionOrLater("2021.6.0", version));
    }

    @Test
    public void testScanBatch() throws Exception {
        IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();

        checkVersionForRapidScan(blackDuckServicesFactory);

        RapidScanService rapidScanService = blackDuckServicesFactory.createRapidScanService();
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
        String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(new UploadBatch(uploadTarget), timeout, 5);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getViolatingPolicyNames());
    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
            BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();

            checkVersionForRapidScan(blackDuckServicesFactory);

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
