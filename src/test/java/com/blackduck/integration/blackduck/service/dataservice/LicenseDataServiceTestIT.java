package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.bdio.SimpleBdioFactory;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.bdio.model.externalid.ExternalIdFactory;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.enumeration.LicenseType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionLicenseLicensesView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionLicenseView;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class LicenseDataServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingLicenseFromComponentVersion() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        LicenseService licenseService = blackDuckServicesFactory.createLicenseService();

        SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
        ExternalIdFactory externalIdFactory = simpleBdioFactory.getExternalIdFactory();
        ExternalId guavaExternalId = externalIdFactory.createMavenExternalId("com.google.guava", "guava", "20.0");
        Optional<ComponentVersionLicenseView> optionalComplexLicense = licenseService.getComplexLicenseItemFromComponent(guavaExternalId);
        ComponentVersionLicenseView complexLicense = optionalComplexLicense.get();

        assertEquals("Apache License 2.0", complexLicense.getLicenseDisplay());
        assertEquals(LicenseType.DISJUNCTIVE, complexLicense.getType());
        assertEquals(1, complexLicense.getLicenses().size());

        ComponentVersionLicenseLicensesView embeddedLicense = complexLicense.getLicenses().get(0);
        //assertTrue(StringUtils.isNotBlank(embeddedLicense.getLicense()));
        assertEquals("Apache License 2.0", embeddedLicense.getLicenseDisplay());
        assertEquals("Apache License 2.0", embeddedLicense.getName());
        assertEquals("OPEN_SOURCE", embeddedLicense.getOwnership().name());
        //assertNull(embeddedLicense.getType());
        assertEquals(0, embeddedLicense.getLicenses().size());

        System.out.println(complexLicense);
    }

    @Test
    public void testGetLicenseUrlByName() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        LicenseService licenseService = blackDuckServicesFactory.createLicenseService();
        String licenseName = ".NETZ GPL 2.0 With Exception License";
        licenseService.getLicenseUrlByLicenseName(licenseName).get().string();
    }

    @Test
    public void testGetLicenseUrlByNameReturnsEmptyOptionalOnBadLicenseName() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        LicenseService licenseService = blackDuckServicesFactory.createLicenseService();
        String licenseName = "fakeLicenseName";
        Assertions.assertFalse(licenseService.getLicenseUrlByLicenseName(licenseName).isPresent());
    }

}
