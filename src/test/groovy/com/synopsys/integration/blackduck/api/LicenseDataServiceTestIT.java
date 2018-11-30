package com.synopsys.integration.blackduck.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseCodeSharingType;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseOwnershipType;
import com.synopsys.integration.blackduck.api.generated.view.ComplexLicenseView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.LicenseService;

@Tag("integration")
public class LicenseDataServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingLicenseFromComponentVersion() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final LicenseService licenseService = blackDuckServicesFactory.createLicenseService();

        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
        final ExternalId guavaExternalId = simpleBdioFactory.createMavenExternalId("com.google.guava", "guava", "20.0");
        final Optional<ComplexLicenseView> optionalComplexLicense = licenseService.getComplexLicenseItemFromComponent(guavaExternalId);
        final ComplexLicenseView complexLicense = optionalComplexLicense.get();

        assertEquals(LicenseCodeSharingType.PERMISSIVE, complexLicense.getCodeSharing());
        assertTrue(StringUtils.isNotBlank(complexLicense.getLicense()));
        assertEquals("Apache License 2.0", complexLicense.getName());
        assertEquals(LicenseOwnershipType.OPEN_SOURCE, complexLicense.getOwnership());
        assertNull(complexLicense.getType());
        assertEquals(0, complexLicense.getLicenses().size());

        System.out.println(complexLicense);
    }

}
