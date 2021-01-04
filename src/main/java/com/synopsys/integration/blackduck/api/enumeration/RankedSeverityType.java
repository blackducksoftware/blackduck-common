/**
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
package com.synopsys.integration.blackduck.api.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

public enum RankedSeverityType {
    UNSPECIFIED(PolicyRuleSeverityType.UNSPECIFIED),
    TRIVIAL(PolicyRuleSeverityType.TRIVIAL),
    MINOR(PolicyRuleSeverityType.MINOR),
    MAJOR(PolicyRuleSeverityType.MAJOR),
    CRITICAL(PolicyRuleSeverityType.CRITICAL),
    BLOCKER(PolicyRuleSeverityType.BLOCKER);

    private final PolicyRuleSeverityType policyRuleSeverityType;

    private RankedSeverityType(PolicyRuleSeverityType unrankedSeverityType) {
        this.policyRuleSeverityType = unrankedSeverityType;
    }

    public static List<PolicyRuleSeverityType> getRankedValues() {
        return Arrays
                   .stream(values())
                   .map(t -> t.policyRuleSeverityType)
                   .collect(Collectors.toList());
    }
}
