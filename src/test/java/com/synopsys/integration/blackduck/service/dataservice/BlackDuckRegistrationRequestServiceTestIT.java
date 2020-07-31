package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckRegistrationRequestServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingRegistrationId() throws Exception {
        BlackDuckServicesFactory blackDuckDataServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        BlackDuckRegistrationService blackDuckRegistrationRequestService = blackDuckDataServicesFactory.createBlackDuckRegistrationService();
        String registrationId = blackDuckRegistrationRequestService.getRegistrationId();
        assertTrue(StringUtils.isNotBlank(registrationId));
        System.out.println(registrationId);
    }

}
