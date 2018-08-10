package com.blackducksoftware.integration.hub.service.model;

import java.util.List;

import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV2View;

public class ComponentVersionVulnerabilities {
    private final ComponentVersionView componentVersionView;
    private final List<VulnerabilityV2View> vulnerabilities;

    public ComponentVersionVulnerabilities(final ComponentVersionView componentVersionView, final List<VulnerabilityV2View> vulnerabilities) {
        this.componentVersionView = componentVersionView;
        this.vulnerabilities = vulnerabilities;
    }

    public ComponentVersionView getComponentVersionView() {
        return componentVersionView;
    }

    public List<VulnerabilityV2View> getVulnerabilities() {
        return vulnerabilities;
    }

}
