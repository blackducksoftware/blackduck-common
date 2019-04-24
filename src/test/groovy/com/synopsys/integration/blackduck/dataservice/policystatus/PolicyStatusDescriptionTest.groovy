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
import static org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(TimingExtension.class)
class PolicyStatusDescriptionTest {
    @Test
    void getCountTest() {
        VersionBomPolicyStatusView policyStatusItem = createVersionBomPolicyStatusView();
        final PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem)

        int expectedInViolationOverall = 4
        int expectedNotInViolationOverall = 1
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

    @Test
    public void testMessageHandlesSingularComponents() {
        VersionBomPolicyStatusView policyStatusItem = createVersionBomPolicyStatusView();
        final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem)
        String message = policyStatusDescription.getPolicyStatusMessage()
        assertEquals("Black Duck found: 4 components in violation (Policy Severity counts: 3 matches have a severity level of BLOCKER, 1 match has a severity level of TRIVIAL), 0 components in violation, but overridden, and 1 component not in violation.", message)
        assertNotNull(message)
    }

    private VersionBomPolicyStatusView createVersionBomPolicyStatusView() {
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

        final NameValuePairView notInViolation = new NameValuePairView()
        notInViolation.name = PolicySummaryStatusType.NOT_IN_VIOLATION
        notInViolation.value = 1

        def statuses = []
        statuses.add(inViolation)
        statuses.add(notInViolation)

        final VersionBomPolicyStatusView policyStatusItem = new VersionBomPolicyStatusView()
        policyStatusItem.componentVersionPolicyViolationDetails = componentVersionPolicyViolationDetails
        policyStatusItem.componentVersionStatusCounts = statuses
        policyStatusItem.overallStatus = PolicySummaryStatusType.IN_VIOLATION

        return policyStatusItem
    }

}
