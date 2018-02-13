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
package com.blackducksoftware.integration.hub.service.model;

import java.util.Collections;
import java.util.List;

import com.blackducksoftware.integration.hub.api.enumeration.PolicyRuleConditionType;
import com.blackducksoftware.integration.hub.api.generated.component.PolicyRuleExpression;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;

public class PolicyRuleModel {
    private final PolicyRuleView rule;

    public PolicyRuleModel(final PolicyRuleView rule) {
        this.rule = rule;
    }

    public boolean hasExpressions() {
        return rule != null && rule.expression != null && rule.expression.expressions != null
                && !rule.expression.expressions.isEmpty();
    }

    public List<PolicyRuleExpression> getExpressionList() {
        if (hasExpressions()) {
            return rule.expression.expressions;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasOnlyProjectLevelConditions() {
        boolean hasNonProjectLevelCondition = false;

        for (final PolicyRuleExpression expression : getExpressionList()) {
            final PolicyRuleConditionType condition = PolicyRuleConditionType.valueOf(expression.name);
            if (condition == PolicyRuleConditionType.UNKNOWN_RULE_CONDTION) {
                continue;
            }
            if (condition != PolicyRuleConditionType.PROJECT_TIER
                    && condition != PolicyRuleConditionType.VERSION_PHASE
                    && condition != PolicyRuleConditionType.VERSION_DISTRIBUTION) {
                hasNonProjectLevelCondition = true;
            }
        }

        return !hasNonProjectLevelCondition;
    }
}
