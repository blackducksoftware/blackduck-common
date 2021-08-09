/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class PolicyRuleService extends DataService {
    private final UrlMultipleResponses<PolicyRuleView> policyRulesResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.POLICY_RULES_PATH);

    public PolicyRuleService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<PolicyRuleView> getAllPolicyRules() throws IntegrationException {
        return blackDuckApiClient.getAllResponses(policyRulesResponses);
    }

    public Optional<PolicyRuleView> getPolicyRuleViewByName(String policyRuleName) throws IntegrationException {
        Predicate<PolicyRuleView> predicate = policyRuleView -> policyRuleName.equals(policyRuleView.getName());
        List<PolicyRuleView> allPolicyRules = blackDuckApiClient.getSomeMatchingResponses(policyRulesResponses, predicate, 1);
        for (PolicyRuleView policyRule : allPolicyRules) {
            if (policyRuleName.equals(policyRule.getName())) {
                return Optional.of(policyRule);
            }
        }
        return Optional.empty();
    }

    public HttpUrl createPolicyRule(PolicyRuleView policyRuleView) throws IntegrationException {
        return blackDuckApiClient.post(policyRulesResponses.getUrl(), policyRuleView);
    }

    /**
     * This will create a policy rule that will be violated by the existence of a matching external id in the project's BOM.
     */
    public HttpUrl createPolicyRuleForExternalId(ComponentService componentService, ExternalId externalId, String policyName) throws IntegrationException {
        Optional<ComponentsView> componentSearchResult = componentService.getSingleOrEmptyResult(externalId);
        if (!componentSearchResult.isPresent()) {
            throw new BlackDuckIntegrationException(String.format("The external id (%s) provided could not be found, so no policy can be created for it.", externalId.createExternalId()));
        }

        Optional<ComponentVersionView> componentVersionView = componentService.getComponentVersionView(componentSearchResult.get());
        if (!componentVersionView.isPresent()) {
            throw new BlackDuckIntegrationException(String.format("A component version could not be found for the provided external id (%s), so no policy can be created for it.", externalId.createExternalId()));
        }

        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView.get());
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(policyName);
        policyRuleView.setEnabled(true);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);

        return createPolicyRule(policyRuleView);
    }

}
