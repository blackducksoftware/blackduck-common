/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc.
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

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;

public class PolicyStatusDescription {
    private final PolicyStatusItem policyStatusItem;

    public PolicyStatusDescription(final PolicyStatusItem policyStatusItem) {
        this.policyStatusItem = policyStatusItem;
    }

    public String getPolicyStatusMessage() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The Hub found: ");
        stringBuilder.append(policyStatusItem.getCountInViolation().getValue());
        stringBuilder.append(" components in violation, ");
        stringBuilder.append(policyStatusItem.getCountInViolationOverridden().getValue());
        stringBuilder.append(" components in violation, but overridden, and ");
        stringBuilder.append(policyStatusItem.getCountNotInViolation().getValue());
        stringBuilder.append(" components not in violation.");
        return stringBuilder.toString();
    }

}
