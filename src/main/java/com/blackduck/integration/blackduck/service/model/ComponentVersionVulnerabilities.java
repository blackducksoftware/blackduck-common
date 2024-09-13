/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.VulnerabilityView;

import java.util.List;

public class ComponentVersionVulnerabilities {
    private final ComponentVersionView componentVersionView;
    private final List<VulnerabilityView> vulnerabilities;

    public ComponentVersionVulnerabilities(ComponentVersionView componentVersionView, List<VulnerabilityView> vulnerabilities) {
        this.componentVersionView = componentVersionView;
        this.vulnerabilities = vulnerabilities;
    }

    public ComponentVersionView getComponentVersionView() {
        return componentVersionView;
    }

    public List<VulnerabilityView> getVulnerabilities() {
        return vulnerabilities;
    }

}
