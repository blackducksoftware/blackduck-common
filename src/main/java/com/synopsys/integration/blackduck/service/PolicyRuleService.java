/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.exception.IntegrationException;

public class PolicyRuleService {
    private final BlackDuckService blackDuckService;

    public PolicyRuleService(final BlackDuckService blackDuckService) {
        this.blackDuckService = blackDuckService;
    }

    public PolicyRuleViewV2 getPolicyRuleViewByName(final String policyRuleName) throws IntegrationException {
        final List<PolicyRuleViewV2> allPolicyRules = blackDuckService.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
        for (final PolicyRuleViewV2 policyRule : allPolicyRules) {
            if (policyRuleName.equals(policyRule.getName())) {
                return policyRule;
            }
        }
        throw new DoesNotExistException("This Policy Rule does not exist: " + policyRuleName);
    }

    /**
     * This will create a policy rule that will be violated by the existence of a matching external id in the project's BOM.
     */
    public String createPolicyRuleForExternalId(final ComponentService componentService, final ExternalId externalId, final String policyName) throws IntegrationException {
        final Optional<ComponentVersionView> componentVersionView = componentService.getComponentVersion(externalId);
        if (!componentVersionView.isPresent()) {
            throw new BlackDuckIntegrationException(String.format("The external id (%s) provided could not be found, so no policy can be created for it.", externalId.createExternalId()));
        }

        final PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView.get());
        final PolicyRuleExpressionSetView expressionSet = builder.createPolicyRuleExpressionSetView();

        final PolicyRuleViewV2 policyRuleViewV2 = new PolicyRuleViewV2();
        policyRuleViewV2.setName(policyName);
        policyRuleViewV2.setEnabled(true);
        policyRuleViewV2.setOverridable(true);
        policyRuleViewV2.setExpression(expressionSet);

        return blackDuckService.create(ApiDiscovery.POLICY_RULES_LINK, policyRuleViewV2);
    }

}
