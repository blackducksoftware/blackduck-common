package com.synopsys.integration.blackduck.dataservice.policystatus

import com.synopsys.integration.blackduck.TimingExtension
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.NameValuePairView
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

@ExtendWith(TimingExtension.class)
class PolicyStatusDescriptionTest {
    @Test
    void getCountTest() {
        ProjectVersionPolicyStatusView policyStatusItem = createProjectVersionPolicyStatusView()
        final PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem)

        int expectedInViolationOverall = 4
        int expectedNotInViolationOverall = 1
        int expectedBlockerSeverity = 3
        int expectedTrivialSeverity = 1
        int expectedMajorSeverity = 0
        int actualInViolationOverall = test.getCountOfStatus(PolicyStatusType.IN_VIOLATION)
        int actualNotInViolationOverall = test.getCountOfStatus(PolicyStatusType.NOT_IN_VIOLATION)
        int actualBlockerSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.BLOCKER)
        int actualTrivialSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.TRIVIAL)
        int actualMajorSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.MAJOR)

        assertEquals(expectedInViolationOverall, actualInViolationOverall)
        assertEquals(expectedNotInViolationOverall, actualNotInViolationOverall)
        assertEquals(expectedBlockerSeverity, actualBlockerSeverity)
        assertEquals(expectedTrivialSeverity, actualTrivialSeverity)
        assertEquals(expectedMajorSeverity, actualMajorSeverity)
    }

    @Test
    public void testMessageHandlesSingularComponents() {
        ProjectVersionPolicyStatusView policyStatusItem = createProjectVersionPolicyStatusView();
        final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem)
        String message = policyStatusDescription.getPolicyStatusMessage()
        assertEquals("Black Duck found: 4 components in violation (Policy Severity counts: 1 match has a severity level of TRIVIAL, 3 matches have a severity level of BLOCKER), 0 components in violation, but overridden, and 1 component not in violation.", message)
        assertNotNull(message)
    }

    private ProjectVersionPolicyStatusView createProjectVersionPolicyStatusView() {
        final NameValuePairView blockerViolation = new NameValuePairView()
        blockerViolation.name = PolicyRuleSeverityType.BLOCKER
        blockerViolation.value = 3

        final NameValuePairView trivialViolation = new NameValuePairView()
        trivialViolation.name = PolicyRuleSeverityType.TRIVIAL
        trivialViolation.value = 1

        def violations = []
        violations.add(blockerViolation)
        violations.add(trivialViolation)

        final ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView = new ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView()
        projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView.severityLevels = violations

        final NameValuePairView inViolation = new NameValuePairView()
        inViolation.name = PolicyStatusType.IN_VIOLATION
        inViolation.value = 4

        final NameValuePairView notInViolation = new NameValuePairView()
        notInViolation.name = PolicyStatusType.NOT_IN_VIOLATION
        notInViolation.value = 1

        def statuses = []
        statuses.add(inViolation)
        statuses.add(notInViolation)

        final ProjectVersionPolicyStatusView policyStatusItem = new ProjectVersionPolicyStatusView()
        policyStatusItem.componentVersionPolicyViolationDetails = projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView
        policyStatusItem.componentVersionStatusCounts = statuses
        policyStatusItem.overallStatus = PolicyStatusType.IN_VIOLATION

        return policyStatusItem
    }

}
