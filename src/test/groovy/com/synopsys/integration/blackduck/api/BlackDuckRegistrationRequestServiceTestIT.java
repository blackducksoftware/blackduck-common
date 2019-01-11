package com.synopsys.integration.blackduck.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
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
