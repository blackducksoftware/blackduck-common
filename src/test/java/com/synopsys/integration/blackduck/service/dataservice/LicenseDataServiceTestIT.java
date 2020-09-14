package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionLicenseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseLicensesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class LicenseDataServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingLicenseFromComponentVersion() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        LicenseService licenseService = blackDuckServicesFactory.createLicenseService();

        SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();
        ExternalId guavaExternalId = simpleBdioFactory.createMavenExternalId("com.google.guava", "guava", "20.0");
        Optional<ProjectVersionLicenseView> optionalComplexLicense = licenseService.getComplexLicenseItemFromComponent(guavaExternalId);
        ProjectVersionLicenseView complexLicense = optionalComplexLicense.get();

        assertEquals("Apache License 2.0", complexLicense.getLicenseDisplay());
        assertEquals(ProjectVersionLicenseType.DISJUNCTIVE, complexLicense.getType());
        assertEquals(1, complexLicense.getLicenses().size());

        ProjectVersionLicenseLicensesView embeddedLicense = complexLicense.getLicenses().get(0);
        assertTrue(StringUtils.isNotBlank(embeddedLicense.getLicense()));
        assertEquals("Apache License 2.0", embeddedLicense.getLicenseDisplay());
        assertEquals("Apache License 2.0", embeddedLicense.getName());
        assertEquals("OPEN_SOURCE", embeddedLicense.getOwnership());
        assertNull(embeddedLicense.getType());
        assertEquals(0, embeddedLicense.getLicenses().size());

        System.out.println(complexLicense);
    }

}
