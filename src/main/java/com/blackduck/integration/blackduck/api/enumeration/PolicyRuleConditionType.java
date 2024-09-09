/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.api.enumeration;

public enum PolicyRuleConditionType {
    PROJECT_NAME("Project Name"),
    PROJECT_TAGS("Project Tags"),
    PROJECT_TIER("Project Tier"),
    VERSION_PHASE("Project Phase"),
    VERSION_DISTRIBUTION("Project Distribution Type"),
    SINGLE_VERSION("Component"),
    COMPONENT_USAGE("Component Usage"),
    REVIEW_STATUS("Review Status"),
    LICENSE_FAMILY("License Family"),
    SINGLE_LICENSE("License"),
    RELEASE_DATE("Component Release Date"),
    NEWER_VERSIONS_COUNT("Newer Versions Count"),
    HIGH_SEVERITY_VULN_COUNT("High Severity Vulnerability Count"),
    MEDIUM_SEVERITY_VULN_COUNT("Medium Severity Vulnerability Count"),
    LOW_SEVERITY_VULN_COUNT("Low Severity Vulnerability Count"),
    VULN_LEVEL_OVERALL_SCORE("Overall Score");

    private final String displayValue;

    PolicyRuleConditionType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

}
