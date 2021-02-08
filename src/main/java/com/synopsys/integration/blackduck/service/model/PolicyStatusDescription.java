/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.enumeration.RankedSeverityType;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.NameValuePairView;

public class PolicyStatusDescription {
    private final ProjectVersionPolicyStatusView policyStatusItem;

    private final Map<ProjectVersionComponentPolicyStatusType, ComponentVersionStatusCount> policyStatusCount = new HashMap<>();
    private final Map<PolicyRuleSeverityType, ComponentVersionPolicyViolationCount> policySeverityCount = new HashMap<>();

    public PolicyStatusDescription(ProjectVersionPolicyStatusView policyStatusItem) {
        this.policyStatusItem = policyStatusItem;
        populatePolicySeverityMap();
        populatePolicyStatusMap();
    }

    private void populatePolicySeverityMap() {
        ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView policyViolationDetails = policyStatusItem.getComponentVersionPolicyViolationDetails();
        if (policyViolationDetails != null && ProjectVersionComponentPolicyStatusType.IN_VIOLATION.equals(policyStatusItem.getOverallStatus())) {
            List<NameValuePairView> nameValuePairs = policyViolationDetails.getSeverityLevels();
            if (nameValuePairs != null) {
                for (NameValuePairView nameValuePairView : nameValuePairs) {
                    if (nameValuePairView.getName() != null) {
                        ComponentVersionPolicyViolationCount componentVersionPolicyViolationCount = new ComponentVersionPolicyViolationCount(nameValuePairView);
                        policySeverityCount.put(componentVersionPolicyViolationCount.name, componentVersionPolicyViolationCount);
                    }
                }
            }
        }
    }

    private void populatePolicyStatusMap() {
        List<NameValuePairView> nameValuePairs = policyStatusItem.getComponentVersionStatusCounts();
        if (nameValuePairs != null) {
            for (NameValuePairView nameValuePairView : nameValuePairs) {
                if (nameValuePairView.getName() != null) {
                    ComponentVersionStatusCount componentVersionStatusCount = new ComponentVersionStatusCount(nameValuePairView);
                    policyStatusCount.put(componentVersionStatusCount.name, componentVersionStatusCount);
                }
            }
        }
    }

    public String getPolicyStatusMessage() {
        if (policyStatusItem.getComponentVersionStatusCounts() == null || policyStatusItem.getComponentVersionStatusCounts().size() == 0) {
            return "Black Duck found no components.";
        }

        int inViolationCount = getCountOfStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        int inViolationOverriddenCount = getCountOfStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN);
        int notInViolationCount = getCountOfStatus(ProjectVersionComponentPolicyStatusType.NOT_IN_VIOLATION);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Black Duck found:");
        stringBuilder.append(fixComponentPlural(" %d %s in violation", inViolationCount));
        if (getCountOfStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION) > 0) {
            stringBuilder.append(" (");
            getPolicySeverityMessage(stringBuilder);
            stringBuilder.append(")");
        }
        stringBuilder.append(",");
        stringBuilder.append(fixComponentPlural(" %d %s in violation, but overridden, and", inViolationOverriddenCount));
        stringBuilder.append(fixComponentPlural(" %d %s not in violation.", notInViolationCount));
        return stringBuilder.toString();
    }

    private void getPolicySeverityMessage(StringBuilder stringBuilder) {
        stringBuilder.append("Policy Severity counts: ");
        // let's loop over the actual enum values for a consistently ordered output
        String policySeverityItems = RankedSeverityType.getRankedValues()
                                         .stream()
                                         .filter(policySeverityCount::containsKey)
                                         .map(policyRuleSeverityType -> fixMatchPlural("%d %s a severity level of %s", policySeverityCount.get(policyRuleSeverityType).value, policyRuleSeverityType))
                                         .collect(Collectors.joining(", "));
        stringBuilder.append(policySeverityItems);
    }

    public ComponentVersionStatusCount getCountInViolation() {
        return policyStatusCount.get(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountNotInViolation() {
        return policyStatusCount.get(ProjectVersionComponentPolicyStatusType.NOT_IN_VIOLATION);
    }

    public ComponentVersionStatusCount getCountInViolationOverridden() {
        return policyStatusCount.get(ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN);
    }

    public int getCountOfStatus(ProjectVersionComponentPolicyStatusType overallStatus) {
        ComponentVersionStatusCount count = policyStatusCount.get(overallStatus);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

    public int getCountOfSeverity(PolicyRuleSeverityType severity) {
        ComponentVersionPolicyViolationCount count = policySeverityCount.get(severity);
        if (count == null) {
            return 0;
        }
        return count.value;
    }

    private String fixComponentPlural(String formatString, int count) {
        String label = "components";
        if (count == 1)
            label = "component";
        return String.format(formatString, count, label);
    }

    private String fixMatchPlural(String formatString, int count, PolicyRuleSeverityType policyRuleSeverityType) {
        String label = "matches have";
        if (count == 1)
            label = "match has";
        return String.format(formatString, count, label, policyRuleSeverityType);
    }

}
