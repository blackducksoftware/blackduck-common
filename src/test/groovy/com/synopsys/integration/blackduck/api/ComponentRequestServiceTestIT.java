package com.synopsys.integration.blackduck.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.view.ComponentSearchResultView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;

@Tag("integration")
public class ComponentRequestServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingIntegrationCommon() throws Exception {
        final BlackDuckServicesFactory blackDuckServicesFactory = restConnectionTestHelper.createBlackDuckServicesFactory();
        final ComponentService componentRequestService = blackDuckServicesFactory.createComponentService();
        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        final ExternalId integrationCommonExternalId = simpleBdioFactory.createMavenExternalId("com.blackducksoftware.integration", "integration-common", "15.0.0");
        final Optional<ComponentSearchResultView> componentView = componentRequestService.getExactComponentMatch(integrationCommonExternalId);

        assertTrue(componentView.isPresent());
    }

}
