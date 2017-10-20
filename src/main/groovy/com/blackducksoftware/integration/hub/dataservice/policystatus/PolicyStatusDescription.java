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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
        final ComponentVersionPolicyViolationDetails policyViolationDetails = policyStatusItem.componentVersionPolicyViolationDetails;
        if (policyViolationDetails != null && VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION.equals(policyStatusItem.overallStatus)) {
            final List<ComponentVersionPolicyViolationCount> severityLevels = policyViolationDetails.severityLevels;
            if (policyViolationDetails.severityLevels != null) {
                for (final ComponentVersionPolicyViolationCount count : severityLevels) {
                    policySeverityCount.put(count.name, count);
                }
            }
        }
    }

    private void populatePolicyStatusMap() {
        final List<ComponentVersionStatusCount> versionStatusCounts = policyStatusItem.componentVersionStatusCounts;
        if (versionStatusCounts != null) {
            for (final ComponentVersionStatusCount policyStatus : versionStatusCounts) {
                policyStatusCount.put(policyStatus.name, policyStatus);
            }
        }
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
        stringBuilder.append(" components in violation");
        if (getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION) != 0) {
            stringBuilder.append(" (");
            getPolicySeverityMessage(stringBuilder);
            stringBuilder.append("), ");
        } else {
            stringBuilder.append(", ");
        }
        stringBuilder.append(inViolationOverriddenCount);
        stringBuilder.append(" components in violation, but overridden, and ");
        stringBuilder.append(notInViolationCount);
        stringBuilder.append(" components not in violation.");
        return stringBuilder.toString();
    }

    private void getPolicySeverityMessage(final StringBuilder stringBuilder) {
        final List<String> policySeverityItems = new ArrayList<>();
        stringBuilder.append("Policy Severity counts: ");
        for (final PolicySeverityEnum policySeverityEnum : policySeverityCount.keySet()) {
            final ComponentVersionPolicyViolationCount policySeverity = policySeverityCount.get(policySeverityEnum);
            if (policySeverity != null) {
                policySeverityItems.add(policySeverity.value + " component(s) have a severity level of " + policySeverityEnum.toString());
            }
        }
        stringBuilder.append(StringUtils.join(policySeverityItems, ", "));
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

    public static void main(final String[] args) {
        final ComponentVersionPolicyViolationCount blockerViolation = new ComponentVersionPolicyViolationCount();
        blockerViolation.name = PolicySeverityEnum.BLOCKER;
        blockerViolation.value = 3;

        final ComponentVersionPolicyViolationCount trivialViolation = new ComponentVersionPolicyViolationCount();
        trivialViolation.name = PolicySeverityEnum.TRIVIAL;
        trivialViolation.value = 1;

        final List<ComponentVersionPolicyViolationCount> violations = new ArrayList<>();
        violations.add(blockerViolation);
        violations.add(trivialViolation);

        final ComponentVersionPolicyViolationDetails componentVersionPolicyViolationDetails = new ComponentVersionPolicyViolationDetails();
        componentVersionPolicyViolationDetails.severityLevels = violations;

        final ComponentVersionStatusCount inViolation = new ComponentVersionStatusCount();
        inViolation.name = VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION;
        inViolation.value = 4;

        final List<ComponentVersionStatusCount> statuses = new ArrayList<>();
        statuses.add(inViolation);

        final VersionBomPolicyStatusView policyStatusItem = new VersionBomPolicyStatusView();
        policyStatusItem.componentVersionPolicyViolationDetails = componentVersionPolicyViolationDetails;
        policyStatusItem.componentVersionStatusCounts = statuses;
        policyStatusItem.overallStatus = VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION;

        final PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem);
        final String result = test.getPolicyStatusMessage();
        System.out.println(result);
    }

}
