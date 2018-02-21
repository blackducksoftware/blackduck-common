/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.service.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.enumeration.PolicySeverityType;
import com.blackducksoftware.integration.hub.api.generated.component.ComponentVersionPolicyViolationDetails;
import com.blackducksoftware.integration.hub.api.generated.component.NameValuePairView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusApprovalStatusType;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;

public class PolicyStatusDescription {
    private final VersionBomPolicyStatusView policyStatusItem;

    private final Map<PolicyStatusApprovalStatusType, ComponentVersionStatusCount> policyStatusCount = new HashMap<>();
    private final Map<PolicySeverityType, ComponentVersionPolicyViolationCount> policySeverityCount = new HashMap<>();

    public PolicyStatusDescription(final VersionBomPolicyStatusView policyStatusItem) {
        this.policyStatusItem = policyStatusItem;
        populatePolicySeverityMap();
        populatePolicyStatusMap();
    }

    private void populatePolicySeverityMap() {
        final ComponentVersionPolicyViolationDetails policyViolationDetails = policyStatusItem.componentVersionPolicyViolationDetails;
        if (policyViolationDetails != null && PolicyStatusApprovalStatusType.IN_VIOLATION.equals(policyStatusItem.overallStatus)) {
            final List<NameValuePairView> nameValuePairs = policyViolationDetails.severityLevels;
            if (nameValuePairs != null) {
                for (final NameValuePairView nameValuePairView : nameValuePairs) {
                    if (nameValuePairView.name != null) {
                        final ComponentVersionPolicyViolationCount componentVersionPolicyViolationCount = new ComponentVersionPolicyViolationCount(nameValuePairView);
                        policySeverityCount.put(componentVersionPolicyViolationCount.name, componentVersionPolicyViolationCount);
                    }
                }
            }
        }
    }

    private void populatePolicyStatusMap() {
        final List<NameValuePairView> nameValuePairs = policyStatusItem.componentVersionStatusCounts;
        if (nameValuePairs != null) {
            for (final NameValuePairView nameValuePairView : nameValuePairs) {
                if (nameValuePairView.name != null) {
                    final ComponentVersionStatusCount componentVersionStatusCount = new ComponentVersionStatusCount(nameValuePairView);
                    policyStatusCount.put(componentVersionStatusCount.name, componentVersionStatusCount);
                }
            }
        }
    }

    public String getPolicyStatusMessage() {
        if (policyStatusItem.componentVersionStatusCounts == null || policyStatusItem.componentVersionStatusCounts.size() == 0) {
            return "The Hub found no components.";
        }

        final int inViolationCount = getCountOfStatus(PolicyStatusApprovalStatusType.IN_VIOLATION);
        final int inViolationOverriddenCount = getCountOfStatus(PolicyStatusApprovalStatusType.IN_VIOLATION_OVERRIDDEN);
        final int notInViolationCount = getCountOfStatus(PolicyStatusApprovalStatusType.NOT_IN_VIOLATION);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The Hub found: ");
        stringBuilder.append(inViolationCount);
        stringBuilder.append(" components in violation");
        if (getCountOfStatus(PolicyStatusApprovalStatusType.IN_VIOLATION) != 0) {
            stringBuilder.append(" (");
            getPolicySeverityMessage(stringBuilder);
            stringBuilder.append(")");
        }
        stringBuilder.append(", ");
        stringBuilder.append(inViolationOverriddenCount);
        stringBuilder.append(" components in violation, but overridden, and ");
        stringBuilder.append(notInViolationCount);
        stringBuilder.append(" components not in violation.");
        return stringBuilder.toString();
    }

    private void getPolicySeverityMessage(final StringBuilder stringBuilder) {
        final List<String> policySeverityItems = new ArrayList<>();
        stringBuilder.append("Policy Severity counts: ");
        for (final PolicySeverityType policySeverityEnum : policySeverityCount.keySet()) {
            final ComponentVersionPolicyViolationCount policySeverity = policySeverityCount.get(policySeverityEnum);
            if (policySeverity != null) {
                policySeverityItems.add(policySeverity.value + " component(s) have a severity level of " + policySeverityEnum.toString());
            }
        }
        stringBuilder.append(StringUtils.join(policySeverityItems, ", "));
    }

    public ComponentVersionStatusCount getCountInViolation() {
        return policyStatusCount.get(PolicyStatusApprovalStatusType.IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountNotInViolation() {
        return policyStatusCount.get(PolicyStatusApprovalStatusType.NOT_IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountInViolationOverridden() {
        return policyStatusCount.get(PolicyStatusApprovalStatusType.IN_VIOLATION_OVERRIDDEN);
    }

    public int getCountOfStatus(final PolicyStatusApprovalStatusType overallStatus) {
        final ComponentVersionStatusCount count = policyStatusCount.get(overallStatus);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

    public int getCountOfSeverity(final PolicySeverityType severity) {
        final ComponentVersionPolicyViolationCount count = policySeverityCount.get(severity);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

}
