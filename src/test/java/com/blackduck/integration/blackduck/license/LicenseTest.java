package com.blackduck.integration.blackduck.license;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.LicenseView;
import com.blackduck.integration.blackduck.api.manual.response.BlackDuckStringResponse;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class LicenseTest {
    private IntHttpClientTestHelper helper = new IntHttpClientTestHelper();

    @Test
    public void testGettingMITLicenseText() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = helper.createBlackDuckServicesFactory();
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        Predicate<LicenseView> matchesName = licenseView -> "MIT License".equals(licenseView.getName());

        UrlMultipleResponses<LicenseView> multipleResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.LICENSES_PATH);
        List<LicenseView> licenses = blackDuckApiClient.getSomeMatchingResponses(multipleResponses, matchesName, 10);
        assertEquals(1, licenses.size());

        LicenseView mitLicense = licenses.get(0);
        BlackDuckStringResponse blackDuckStringResponse = blackDuckApiClient.getResponse(mitLicense.metaTextLink());

        assertNotNull(blackDuckStringResponse.string());
        assertTrue(blackDuckStringResponse.string().length() > 10);
    }

}
