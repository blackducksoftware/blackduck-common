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

import com.blackducksoftware.integration.hub.api.policy.ComponentVersionStatusCount;
import com.blackducksoftware.integration.hub.api.policy.VersionBomPolicyStatusView;

public class PolicyStatusDescription {
    private final VersionBomPolicyStatusView policyStatusItem;

    public PolicyStatusDescription(final VersionBomPolicyStatusView policyStatusItem) {
        this.policyStatusItem = policyStatusItem;
    }

    public String getPolicyStatusMessage() {
        if (policyStatusItem.getComponentVersionStatusCounts() == null || policyStatusItem.getComponentVersionStatusCounts().size() == 0) {
            return "The Hub found no components.";
        }

        final ComponentVersionStatusCount inViolation = policyStatusItem.getCountInViolation();
        final ComponentVersionStatusCount inViolationOverridden = policyStatusItem.getCountInViolationOverridden();
        final ComponentVersionStatusCount notInViolation = policyStatusItem.getCountNotInViolation();

        final int inViolationCount = inViolation == null ? 0 : inViolation.getValue();
        final int inViolationOverriddenCount = inViolationOverridden == null ? 0 : inViolationOverridden.getValue();
        final int notInViolationCount = notInViolation == null ? 0 : notInViolation.getValue();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The Hub found: ");
        stringBuilder.append(inViolationCount);
        stringBuilder.append(" components in violation, ");
        stringBuilder.append(inViolationOverriddenCount);
        stringBuilder.append(" components in violation, but overridden, and ");
        stringBuilder.append(notInViolationCount);
        stringBuilder.append(" components not in violation.");
        return stringBuilder.toString();
    }

}
