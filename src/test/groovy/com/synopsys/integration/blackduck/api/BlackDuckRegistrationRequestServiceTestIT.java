package com.synopsys.integration.blackduck.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
public class BlackDuckRegistrationRequestServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingRegistrationId() throws Exception {
        final BlackDuckServicesFactory blackDuckDataServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final BlackDuckRegistrationService blackDuckRegistrationRequestService = blackDuckDataServicesFactory.createBlackDuckRegistrationService();
        final String registrationId = blackDuckRegistrationRequestService.getRegistrationId();
        assertTrue(StringUtils.isNotBlank(registrationId));
        System.out.println(registrationId);
    }

}
