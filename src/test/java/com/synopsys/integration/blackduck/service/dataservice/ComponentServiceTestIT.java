package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComponentServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingIntegrationCommon() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        ExternalId integrationCommonExternalId = externalIdFactory.createMavenExternalId("com.blackducksoftware.integration", "integration-common", "15.0.0");
        Optional<ComponentsView> componentView = componentService.getFirstOrEmptyResult(integrationCommonExternalId);

        assertTrue(componentView.isPresent());
    }

    @Test
    public void testGettingComponentVersionWithUpgradeGuidance() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        ExternalId commonsFileuploadExternalId = externalIdFactory.createMavenExternalId("commons-fileupload", "commons-fileupload", "1.2.1");
        Optional<ComponentsView> componentsView = componentService.getFirstOrEmptyResult(commonsFileuploadExternalId);

        assertTrue(componentsView.isPresent());
        String componentVersionHref = componentsView.get().getVersion();
        HttpUrl httpUrl = new HttpUrl(componentVersionHref);

        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
        ComponentVersionView commonsFileupload = blackDuckApiClient.getResponse(httpUrl, ComponentVersionView.class);

        assertTrue(componentService.hasUpgradeGuidance(commonsFileupload));
        assertNotNull(componentService.getUpgradeGuidance(commonsFileupload).get());
    }

    @Test
    public void testGettingComponentVersionWithRemediation() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        ExternalId commonsFileuploadExternalId = externalIdFactory.createMavenExternalId("commons-fileupload", "commons-fileupload", "1.2.1");
        Optional<ComponentsView> componentsView = componentService.getFirstOrEmptyResult(commonsFileuploadExternalId);

        assertTrue(componentsView.isPresent());
        String componentVersionHref = componentsView.get().getVersion();
        HttpUrl httpUrl = new HttpUrl(componentVersionHref);

        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
        ComponentVersionView commonsFileupload = blackDuckApiClient.getResponse(httpUrl, ComponentVersionView.class);

        boolean hasRemediationInformation = componentService.hasRemediationInformation(commonsFileupload);
        if (hasRemediationInformation) {
            System.out.println("you have remediation info - you must be an old Black Duck");
            assertTrue(componentService.getRemediationInformation(commonsFileupload).isPresent());
        } else {
            System.out.println("you don't have remediation info - you must be a new Black Duck");
            assertFalse(componentService.getRemediationInformation(commonsFileupload).isPresent());
        }
    }

    @Test
    public void testOriginIdMismatch() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        ExternalId cyclerExternalId = externalIdFactory.createNameVersionExternalId(Forge.PYPI, "cycler", "0.10.0");
        List<ComponentsView> searchResults = componentService.getAllSearchResults(cyclerExternalId);
        assertEquals(1, searchResults.size());
        assertTrue(searchResults.get(0).getComponentName().equalsIgnoreCase("cycler"));
        assertTrue(searchResults.get(0).getVersionName().equals("0.10.0"));

        Optional<ComponentsView> componentView = componentService.getFirstOrEmptyResult(cyclerExternalId);

        assertTrue(componentView.isPresent());
        assertTrue(componentView.get().getComponentName().equalsIgnoreCase("cycler"));
        assertTrue(componentView.get().getVersionName().equals("0.10.0"));

        componentView = componentService.getSingleOrEmptyResult(cyclerExternalId);

        assertTrue(componentView.isPresent());
        assertTrue(componentView.get().getComponentName().equalsIgnoreCase("cycler"));
        assertTrue(componentView.get().getVersionName().equals("0.10.0"));
    }

}
