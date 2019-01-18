/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.api.enumeration;

public enum PolicyRuleConditionType {
    PROJECT_NAME(PolicyRuleCategoryType.PROJECT, "Project Name"),
    PROJECT_TAGS(PolicyRuleCategoryType.PROJECT, "Project Tags"),
    PROJECT_TIER(PolicyRuleCategoryType.PROJECT, "Project Tier"),
    VERSION_PHASE(PolicyRuleCategoryType.PROJECT, "Project Phase"),
    VERSION_DISTRIBUTION(PolicyRuleCategoryType.PROJECT, "Project Distribution Type"),
    SINGLE_VERSION(PolicyRuleCategoryType.COMPONENT, "Component"),
    COMPONENT_USAGE(PolicyRuleCategoryType.COMPONENT, "Component Usage"),
    REVIEW_STATUS(PolicyRuleCategoryType.COMPONENT, "Review Status"),
    LICENSE_FAMILY(PolicyRuleCategoryType.COMPONENT, "License Family"),
    SINGLE_LICENSE(PolicyRuleCategoryType.COMPONENT, "License"),
    RELEASE_DATE(PolicyRuleCategoryType.COMPONENT, "Component Release Date"),
    NEWER_VERSIONS_COUNT(PolicyRuleCategoryType.COMPONENT, "Newer Versions Count"),
    HIGH_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "High Severity Vulnerability Count"),
    MEDIUM_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "Medium Severity Vulnerability Count"),
    LOW_SEVERITY_VULN_COUNT(PolicyRuleCategoryType.COMPONENT, "Low Severity Vulnerability Count"),
    UNKNOWN_RULE_CONDTION(null, "Unknown Rule Condition");

    private final PolicyRuleCategoryType policyRuleCategory;
    private final String displayValue;

    PolicyRuleConditionType(final PolicyRuleCategoryType policyRuleCategory, final String displayValue) {
        this.policyRuleCategory = policyRuleCategory;
        this.displayValue = displayValue;
    }

    public PolicyRuleCategoryType getPolicyRuleCategory() {
        return policyRuleCategory;
    }

    public String getDisplayValue() {
        return displayValue;
    }

}
