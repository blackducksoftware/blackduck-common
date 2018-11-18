/**
 * hub-common
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

import java.io.IOException;
import java.util.List;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class PolicyRuleService {
    private final HubService hubService;

    public PolicyRuleService(final HubService hubService) {
        this.hubService = hubService;
    }

    public PolicyRuleViewV2 getPolicyRuleViewByName(final String policyRuleName) throws IntegrationException {
        final List<PolicyRuleViewV2> allPolicyRules = hubService.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
        for (final PolicyRuleViewV2 policyRule : allPolicyRules) {
            if (policyRuleName.equals(policyRule.getName())) {
                return policyRule;
            }
        }
        throw new DoesNotExistException("This Policy Rule does not exist: " + policyRuleName);
    }

    public String createPolicyRule(final PolicyRuleViewV2 policyRuleViewV2) throws IntegrationException {
        final String json = hubService.convertToJson(policyRuleViewV2);
        final Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(json);
        return hubService.executePostRequestAndRetrieveURL(ApiDiscovery.POLICY_RULES_LINK, requestBuilder);
    }

    /**
     * This will create a policy rule that will be violated by the existence of a matching external id in the project's BOM.
     */
    public String createPolicyRuleForExternalId(final ComponentService componentService, final ExternalId externalId, final String policyName) throws IntegrationException {
        final ComponentVersionView componentVersionView = componentService.getComponentVersion(externalId);

        final PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView);
        final PolicyRuleExpressionSetView expressionSet = builder.createPolicyRuleExpressionSetView();

        final PolicyRuleViewV2 policyRuleViewV2 = new PolicyRuleViewV2();
        policyRuleViewV2.setName(policyName);
        policyRuleViewV2.setEnabled(true);
        policyRuleViewV2.setOverridable(true);
        policyRuleViewV2.setExpression(expressionSet);

        return createPolicyRule(policyRuleViewV2);
    }

    public void updatePolicyRule(final PolicyRuleViewV2 policyRuleView) throws IntegrationException {
        final String json = hubService.convertToJson(policyRuleView);
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.PUT).bodyContent(new StringBodyContent(json)).uri(policyRuleView.getHref().orElse(null));
        try (Response response = hubService.executeRequest(requestBuilder.build())) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deletePolicyRule(final PolicyRuleViewV2 policyRuleView) throws IntegrationException {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.DELETE).uri(policyRuleView.getHref().orElse(null));
        try (Response response = hubService.executeRequest(requestBuilder.build())) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
