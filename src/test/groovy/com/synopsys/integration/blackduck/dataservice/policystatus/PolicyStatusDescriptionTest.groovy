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
 * under the License.*/
package com.synopsys.integration.blackduck.dataservice.policystatus

import static org.junit.jupiter.api.Assertions.*;

import com.synopsys.integration.blackduck.api.enumeration.PolicySeverityType
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionPolicyViolationDetails
import com.synopsys.integration.blackduck.api.generated.component.NameValuePairView
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription
import org.junit.jupiter.api.Test

class PolicyStatusDescriptionTest {
    @Test
    void getCountTest() {
        final NameValuePairView blockerViolation = new NameValuePairView()
        blockerViolation.name = PolicySeverityType.BLOCKER
        blockerViolation.value = 3

        final NameValuePairView trivialViolation = new NameValuePairView()
        trivialViolation.name = PolicySeverityType.TRIVIAL
        trivialViolation.value = 1

        def violations = []
        violations.add(blockerViolation)
        violations.add(trivialViolation)

        final ComponentVersionPolicyViolationDetails componentVersionPolicyViolationDetails = new ComponentVersionPolicyViolationDetails()
        componentVersionPolicyViolationDetails.severityLevels = violations

        final NameValuePairView inViolation = new NameValuePairView()
        inViolation.name = PolicySummaryStatusType.IN_VIOLATION
        inViolation.value = 4

        def statuses = []
        statuses.add(inViolation)

        final VersionBomPolicyStatusView policyStatusItem = new VersionBomPolicyStatusView()
        policyStatusItem.componentVersionPolicyViolationDetails = componentVersionPolicyViolationDetails
        policyStatusItem.componentVersionStatusCounts = statuses
        policyStatusItem.overallStatus = PolicySummaryStatusType.IN_VIOLATION

        final PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem)

        int expectedInViolationOverall = 4
        int expectedNotInViolationOverall = 0
        int expectedBlockerSeverity = 3
        int expectedTrivialSeverity = 1
        int expectedMajorSeverity = 0
        int actualInViolationOverall = test.getCountOfStatus(PolicySummaryStatusType.IN_VIOLATION)
        int actualNotInViolationOverall = test.getCountOfStatus(PolicySummaryStatusType.NOT_IN_VIOLATION)
        int actualBlockerSeverity = test.getCountOfSeverity(PolicySeverityType.BLOCKER)
        int actualTrivialSeverity = test.getCountOfSeverity(PolicySeverityType.TRIVIAL)
        int actualMajorSeverity = test.getCountOfSeverity(PolicySeverityType.MAJOR)

        assertEquals(expectedInViolationOverall, actualInViolationOverall)
        assertEquals(expectedNotInViolationOverall, actualNotInViolationOverall)
        assertEquals(expectedBlockerSeverity, actualBlockerSeverity)
        assertEquals(expectedTrivialSeverity, actualTrivialSeverity)
        assertEquals(expectedMajorSeverity, actualMajorSeverity)
    }
}
