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
package com.blackducksoftware.integration.hub.dataservice.policystatus

import org.junit.Test

class PolicyStatusDescriptionTest {

    @Test
    void getCountTest() {
        final ComponentVersionPolicyViolationCount blockerViolation = new ComponentVersionPolicyViolationCount()
        blockerViolation.name = PolicySeverityEnum.BLOCKER
        blockerViolation.value = 3

        final ComponentVersionPolicyViolationCount trivialViolation = new ComponentVersionPolicyViolationCount()
        trivialViolation.name = PolicySeverityEnum.TRIVIAL
        trivialViolation.value = 1

        def violations = []
        violations.add(blockerViolation)
        violations.add(trivialViolation)

        final ComponentVersionPolicyViolationDetails componentVersionPolicyViolationDetails = new ComponentVersionPolicyViolationDetails()
        componentVersionPolicyViolationDetails.severityLevels = violations

        final ComponentVersionStatusCount inViolation = new ComponentVersionStatusCount()
        inViolation.name = VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION
        inViolation.value = 4

        def statuses = []
        statuses.add(inViolation)

        final VersionBomPolicyStatusView policyStatusItem = new VersionBomPolicyStatusView()
        policyStatusItem.componentVersionPolicyViolationDetails = componentVersionPolicyViolationDetails
        policyStatusItem.componentVersionStatusCounts = statuses
        policyStatusItem.overallStatus = VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION

        final PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem)

        int expectedInViolationOverall = 4
        int expectedNotInViolationOverall = 0
        int expectedBlockerSeverity = 3
        int expectedTrivialSeverity = 1
        int expectedMajorSeverity = 0
        int actualInViolationOverall = test.getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION)
        int actualNotInViolationOverall = test.getCountOfStatus(VersionBomPolicyStatusOverallStatusEnum.NOT_IN_VIOLATION)
        int actualBlockerSeverity = test.getCountOfSeverity(PolicySeverityEnum.BLOCKER)
        int actualTrivialSeverity = test.getCountOfSeverity(PolicySeverityEnum.TRIVIAL)
        int actualMajorSeverity = test.getCountOfSeverity(PolicySeverityEnum.MAJOR)

        Assert.assertEquals(expectedInViolationOverall, actualInViolationOverall)
        Assert.assertEquals(expectedNotInViolationOverall, actualNotInViolationOverall)
        Assert.assertEquals(expectedBlockerSeverity, actualBlockerSeverity)
        Assert.assertEquals(expectedTrivialSeverity, actualTrivialSeverity)
        Assert.assertEquals(expectedMajorSeverity, actualMajorSeverity)
    }
}
