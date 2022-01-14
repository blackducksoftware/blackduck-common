package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionLicenseLicensesView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionLicenseView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

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
    public void testGetLicenseUrlByNameThrowsExceptionOnBadLicenseName() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        LicenseService licenseService = blackDuckServicesFactory.createLicenseService();
        String licenseName = "fakeLicenseName";
        Assertions.assertThrows(IntegrationException.class, () -> licenseService.getLicenseUrlByLicenseName(licenseName).get().string());
    }

}
