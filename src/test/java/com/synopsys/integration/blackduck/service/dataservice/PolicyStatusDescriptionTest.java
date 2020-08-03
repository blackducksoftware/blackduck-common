package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.NameValuePairView;
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(TimingExtension.class)
public class PolicyStatusDescriptionTest {
    @Test
    public void getCountTest() {
        ProjectVersionPolicyStatusView policyStatusItem = createProjectVersionPolicyStatusView();
        PolicyStatusDescription test = new PolicyStatusDescription(policyStatusItem);

        int expectedInViolationOverall = 4;
        int expectedNotInViolationOverall = 1;
        int expectedBlockerSeverity = 3;
        int expectedTrivialSeverity = 1;
        int expectedMajorSeverity = 0;
        int actualInViolationOverall = test.getCountOfStatus(PolicyStatusType.IN_VIOLATION);
        int actualNotInViolationOverall = test.getCountOfStatus(PolicyStatusType.NOT_IN_VIOLATION);
        int actualBlockerSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.BLOCKER);
        int actualTrivialSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.TRIVIAL);
        int actualMajorSeverity = test.getCountOfSeverity(PolicyRuleSeverityType.MAJOR);

        Assertions.assertEquals(expectedInViolationOverall, actualInViolationOverall);
        Assertions.assertEquals(expectedNotInViolationOverall, actualNotInViolationOverall);
        Assertions.assertEquals(expectedBlockerSeverity, actualBlockerSeverity);
        Assertions.assertEquals(expectedTrivialSeverity, actualTrivialSeverity);
        Assertions.assertEquals(expectedMajorSeverity, actualMajorSeverity);
    }

    @Test
    public void testMessageHandlesSingularComponents() {
        ProjectVersionPolicyStatusView policyStatusItem = createProjectVersionPolicyStatusView();
        PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
        String message = policyStatusDescription.getPolicyStatusMessage();
        Assertions.assertEquals("Black Duck found: 4 components in violation (Policy Severity counts: 1 match has a severity level of TRIVIAL, 3 matches have a severity level of BLOCKER), 0 components in violation, but overridden, and 1 component not in violation.", message);
        Assertions.assertNotNull(message);
    }

    private ProjectVersionPolicyStatusView createProjectVersionPolicyStatusView() {
        NameValuePairView blockerViolation = new NameValuePairView();
        blockerViolation.setName(PolicyRuleSeverityType.BLOCKER.name());
        blockerViolation.setValue(3);

        NameValuePairView trivialViolation = new NameValuePairView();
        trivialViolation.setName(PolicyRuleSeverityType.TRIVIAL.name());
        trivialViolation.setValue(1);

        List<NameValuePairView> violations = new ArrayList<>();
        violations.add(blockerViolation);
        violations.add(trivialViolation);

        ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView = new ProjectVersionPolicyStatusComponentVersionPolicyViolationDetailsView();
        projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView.setSeverityLevels(violations);

        NameValuePairView inViolation = new NameValuePairView();
        inViolation.setName(PolicyStatusType.IN_VIOLATION.name());
        inViolation.setValue(4);

        NameValuePairView notInViolation = new NameValuePairView();
        notInViolation.setName(PolicyStatusType.NOT_IN_VIOLATION.name());
        notInViolation.setValue(1);

        List<NameValuePairView> statuses = new ArrayList<>();
        statuses.add(inViolation);
        statuses.add(notInViolation);

        ProjectVersionPolicyStatusView policyStatusItem = new ProjectVersionPolicyStatusView();
        policyStatusItem.setComponentVersionPolicyViolationDetails(projectVersionPolicyStatusComponentVersionPolicyViolationDetailsView);
        policyStatusItem.setComponentVersionStatusCounts(statuses);
        policyStatusItem.setOverallStatus(PolicyStatusType.IN_VIOLATION);

        return policyStatusItem;
    }

}
