package com.synopsys.integration.blackduck.service.dataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class PolicyRuleServiceTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();

    @Test
    public void testGettingPolicyRuleByName() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
        PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        List<HttpUrl> testPolicies = new LinkedList<>();
        int i = 0;
        while (i < 10) {
            // Create extra policies
            PolicyRuleView extraPolicyRuleView = createTestPolicy("Test Rule " + UUID.randomUUID());
            HttpUrl extraPolicyRuleURL = policyRuleService.createPolicyRule(extraPolicyRuleView);
            testPolicies.add(extraPolicyRuleURL);
            i++;
        }

        // Create the policy we want to look for
        String nameToLookFor = "Looking for this Rule " + UUID.randomUUID();
        PolicyRuleView policyRuleViewToLookFor = createTestPolicy(nameToLookFor);
        HttpUrl policyRuleToLookForURL = policyRuleService.createPolicyRule(policyRuleViewToLookFor);
        testPolicies.add(policyRuleToLookForURL);

        try {
            // Look for the policy by name and make sure the one found is correct
            Optional<PolicyRuleView> policyRuleViewByName = policyRuleService.getPolicyRuleViewByName(nameToLookFor);
            assertTrue(policyRuleViewByName.isPresent());
            PolicyRuleView foundPolicyRuleView = policyRuleViewByName.get();
            assertEquals(nameToLookFor, foundPolicyRuleView.getName());
            assertEquals(PolicyRuleCategoryType.SECURITY, foundPolicyRuleView.getCategory());
            assertEquals(false, foundPolicyRuleView.getEnabled());
        } finally {
            // Cleanup the test policies
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            for (HttpUrl policyRuleURL : testPolicies) {
                blackDuckApiClient.delete(policyRuleURL);
            }
        }
    }

    private PolicyRuleView createTestPolicy(String name) throws BlackDuckIntegrationException {
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addHighSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType.GT, 0);
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setCategory(PolicyRuleCategoryType.SECURITY);
        policyRuleView.setDescription("This is a policy that is used for testing in PolicyRuleServiceTestIT.");
        policyRuleView.setName(name);
        policyRuleView.setEnabled(false);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);

        return policyRuleView;
    }

}
