package com.synopsys.integration.blackduck.service.dataservice;

import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ScanReadinessView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckScanReadinessServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingScanReadiness() throws Exception {
        BlackDuckServicesFactory blackDuckDataServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckScanReadinessService readinessService = blackDuckDataServicesFactory.createScanReadinessService();
        ScanReadinessView scanReadinessView = null;
        try {
            scanReadinessView = readinessService.getScanReadiness();
        } catch (IntegrationException e) {
            Assume.assumeNoException(e); // skip test if using old BD version that doesn't have readiness endpoint
        }
        Assertions.assertNotNull(scanReadinessView.getReadiness());
    }

}
