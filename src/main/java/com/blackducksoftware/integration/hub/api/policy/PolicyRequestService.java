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
package com.blackducksoftware.integration.hub.api.policy;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_POLICY_RULES;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.components.PolicyRuleConditionEnum;
import com.blackducksoftware.integration.hub.model.view.components.PolicyRuleExpression;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class PolicyRequestService extends HubResponseService {
    private static final List<String> POLICY_RULE_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_POLICY_RULES);

    public PolicyRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<PolicyRuleView> getAllPolicyRules() throws IntegrationException {
        final HubPagedRequest request = getHubRequestFactory().createPagedRequest(POLICY_RULE_SEGMENTS);

        final List<PolicyRuleView> allPolicyRuleItems = getAllItems(request, PolicyRuleView.class);
        return allPolicyRuleItems;
    }

    public boolean hasOnlyProjectLevelConditions(final PolicyRuleView policyRuleView) {
        boolean hasNonProjectLevelCondition = false;
        if (policyRuleView.expression != null && policyRuleView.expression.expressions != null
                && !policyRuleView.expression.expressions.isEmpty()) {
            for (final PolicyRuleExpression expression : policyRuleView.expression.expressions) {
                final PolicyRuleConditionEnum condition = PolicyRuleConditionEnum.valueOf(expression.name);
                if (condition == PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION) {
                    continue;
                }
                if (condition != PolicyRuleConditionEnum.PROJECT_TIER
                        && condition != PolicyRuleConditionEnum.VERSION_PHASE
                        && condition != PolicyRuleConditionEnum.VERSION_DISTRIBUTION) {
                    hasNonProjectLevelCondition = true;
                }
            }
        }

        return !hasNonProjectLevelCondition;
    }

}
