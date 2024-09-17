/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.api.enumeration;

import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RankedSeverityType {
    UNSPECIFIED(PolicyRuleSeverityType.UNSPECIFIED),
    TRIVIAL(PolicyRuleSeverityType.TRIVIAL),
    OK(PolicyRuleSeverityType.OK),
    MINOR(PolicyRuleSeverityType.MINOR),
    MAJOR(PolicyRuleSeverityType.MAJOR),
    CRITICAL(PolicyRuleSeverityType.CRITICAL),
    BLOCKER(PolicyRuleSeverityType.BLOCKER);

    private final PolicyRuleSeverityType policyRuleSeverityType;

    RankedSeverityType(PolicyRuleSeverityType unrankedSeverityType) {
        this.policyRuleSeverityType = unrankedSeverityType;
    }

    public static List<PolicyRuleSeverityType> getRankedValues() {
        return Arrays
                   .stream(values())
                   .map(t -> t.policyRuleSeverityType)
                   .collect(Collectors.toList());
    }

}
