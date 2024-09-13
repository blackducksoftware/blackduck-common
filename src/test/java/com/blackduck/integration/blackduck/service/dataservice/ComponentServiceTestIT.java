package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.bdio.model.Forge;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.bdio.model.externalid.ExternalIdFactory;
import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComponentServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();
    private final BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
    private final ComponentService componentService = blackDuckServicesFactory.createComponentService();
    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    public ComponentServiceTestIT() throws IntegrationException {}

    @Test
    public void testGettingIntegrationCommon() throws Exception {
        ExternalId integrationCommonExternalId = externalIdFactory.createMavenExternalId("com.blackducksoftware.integration", "integration-common", "15.0.0");
        Optional<ComponentsView> componentView = componentService.getFirstOrEmptyResult(integrationCommonExternalId);

        assertTrue(componentView.isPresent());
    }

    @Test
    public void testGettingComponentVersionWithUpgradeGuidance() throws IntegrationException {
        ComponentVersionView commonsFileupload = retrieveCommonsFileupload(componentService);

        assertNotNull(componentService.getUpgradeGuidance(commonsFileupload).get());
    }

    @Test
    public void testOriginIdMismatch() throws Exception {
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

    @NotNull
    private ComponentVersionView retrieveCommonsFileupload(ComponentService componentService) throws IntegrationException {
        ExternalId commonsFileuploadExternalId = externalIdFactory.createMavenExternalId("commons-fileupload", "commons-fileupload", "1.2.1");
        Optional<ComponentsView> componentsView = componentService.getFirstOrEmptyResult(commonsFileuploadExternalId);
        return componentService.getComponentVersionView(componentsView.get()).get();
    }

}
