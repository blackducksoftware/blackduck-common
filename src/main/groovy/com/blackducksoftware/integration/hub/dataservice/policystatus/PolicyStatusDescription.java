/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice.policystatus;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.model.enumeration.PolicySeverityEnum;
import com.blackducksoftware.integration.hub.model.enumeration.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.model.view.components.ComponentVersionPolicyViolationCount;
import com.blackducksoftware.integration.hub.model.view.components.ComponentVersionPolicyViolationDetails;
import com.blackducksoftware.integration.hub.model.view.components.ComponentVersionStatusCount;

public class PolicyStatusDescription {
    private final VersionBomPolicyStatusView policyStatusItem;

    private final Map<VersionBomPolicyStatusOverallStatusEnum, ComponentVersionStatusCount> policyStatusCount = new HashMap<>();
    private final Map<PolicySeverityEnum, ComponentVersionPolicyViolationCount> policySeverityCount = new HashMap<>();

    public PolicyStatusDescription(final VersionBomPolicyStatusView policyStatusItem) {
        this.policyStatusItem = policyStatusItem;
        populatePolicySeverityMap();
        populatePolicyStatusMap();
    }

    private void populatePolicySeverityMap() {
        for (final PolicySeverityEnum policySeverity : PolicySeverityEnum.values()) {
            policySeverityCount.put(policySeverity, getSeverityCount(policySeverity));
        }
    }

    private void populatePolicyStatusMap() {
        for (final VersionBomPolicyStatusOverallStatusEnum policyStatus : VersionBomPolicyStatusOverallStatusEnum.values()) {
            policyStatusCount.put(policyStatus, getStatusCount(policyStatus));
        }
    }

    private ComponentVersionPolicyViolationCount getSeverityCount(final PolicySeverityEnum policySeverity) {
        if (policyStatusItem.componentVersionPolicyViolationDetails == null || !VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION.equals(policyStatusItem.overallStatus)) {
            return null;
        }

        final ComponentVersionPolicyViolationDetails policyViolationDetails = policyStatusItem.componentVersionPolicyViolationDetails;
        if (policyViolationDetails.severityLevels != null) {
            for (final ComponentVersionPolicyViolationCount count : policyViolationDetails.severityLevels) {
                if (policySeverity == count.name) {
                    return count;
                }
            }
        }

        return null;
    }

    private ComponentVersionStatusCount getStatusCount(final VersionBomPolicyStatusOverallStatusEnum overallStatus) {
        if (policyStatusItem.componentVersionStatusCounts == null || policyStatusItem.componentVersionStatusCounts.isEmpty()) {
            return null;
        }
        for (final ComponentVersionStatusCount count : policyStatusItem.componentVersionStatusCounts) {
            if (overallStatus == count.name) {
                return count;
            }
        }
        return null;
    }

    public String getPolicyStatusMessage() {
        if (policyStatusItem.componentVersionStatusCounts == null || policyStatusItem.componentVersionStatusCounts.size() == 0) {
            return "The Hub found no components.";
        }

        final int inViolationCount = getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION);
        final int inViolationOverriddenCount = getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION_OVERRIDDEN);
        final int notInViolationCount = getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.NOT_IN_VIOLATION);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The Hub found: ");
        stringBuilder.append(inViolationCount);
        stringBuilder.append(" components in violation (");
        stringBuilder.append(getPolicySeverityMessage().trim());
        stringBuilder.append("), ");
        stringBuilder.append(inViolationOverriddenCount);
        stringBuilder.append(" components in violation, but overridden, and ");
        stringBuilder.append(notInViolationCount);
        stringBuilder.append(" components not in violation.");
        return stringBuilder.toString();
    }

    private String getPolicySeverityMessage() {
        if (policyStatusCount.get(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION).value == 0) {
            return "No policy violation's to check for severity.";
        }

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Policy Severity counts: ");
        for (final PolicySeverityEnum policySeverityEnum : policySeverityCount.keySet()) {
            final ComponentVersionPolicyViolationCount policySeverity = policySeverityCount.get(policySeverityEnum);
            if (policySeverity != null) {
                stringBuilder.append(policySeverity.value);
                stringBuilder.append(" component(s) have a severity level of ");
                stringBuilder.append(policySeverityEnum.toString());
                stringBuilder.append(". ");
            }
        }

        return stringBuilder.toString();
    }

    public ComponentVersionStatusCount getCountInViolation() {
        return policyStatusCount.get(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountNotInViolation() {
        return policyStatusCount.get(VersionBomPolicyStatusOverallStatusEnum.NOT_IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountInViolationOverridden() {
        return policyStatusCount.get(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION_OVERRIDDEN);
    }

    public int getCountOfStatus(final VersionBomPolicyStatusOverallStatusEnum overallStatus) {
        final ComponentVersionStatusCount count = policyStatusCount.get(overallStatus);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

    public int getCountOfSeverity(final PolicySeverityEnum severity) {
        final ComponentVersionPolicyViolationCount count = policySeverityCount.get(severity);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

}
