package com.synopsys.integration.blackduck.dataservice.policystatus

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.enumeration.PolicySeverityType
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionPolicyViolationDetails
import com.synopsys.integration.blackduck.api.generated.component.NameValuePairView
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.assertEquals

@ExtendWith(TimingExtension.class)
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
