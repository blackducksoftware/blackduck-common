package com.synopsys.integration.blackduck.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.view.ComponentSearchResultView;
import com.synopsys.integration.blackduck.rest.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class ComponentServiceTestIT {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testGettingIntegrationCommon() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        ExternalId integrationCommonExternalId = simpleBdioFactory.createMavenExternalId("com.blackducksoftware.integration", "integration-common", "15.0.0");
        Optional<ComponentSearchResultView> componentView = componentService.getExactComponentMatch(integrationCommonExternalId);

        assertTrue(componentView.isPresent());
    }

    @Test
    public void testOriginIdMMismatch() throws Exception {
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();
        SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        ExternalId integrationCommonExternalId = simpleBdioFactory.createNameVersionExternalId(Forge.PYPI, "cycler", "0.10.0");
        Optional<ComponentSearchResultView> componentView = componentService.getExactComponentMatch(integrationCommonExternalId);

        assertTrue(componentView.isPresent());
    }

}
