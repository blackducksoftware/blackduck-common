/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.util.Collections;
import java.util.List;

import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;

public class PolicyRuleModel {
    private final PolicyRuleView rule;

    public PolicyRuleModel(PolicyRuleView rule) {
        this.rule = rule;
    }

    public boolean hasExpressions() {
        return rule != null && rule.getExpression() != null && rule.getExpression().getExpressions() != null && !rule.getExpression().getExpressions().isEmpty();
    }

    public List<PolicyRuleExpressionView> getExpressionList() {
        if (hasExpressions()) {
            return rule.getExpression().getExpressions();
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasOnlyProjectLevelConditions() {
        boolean hasNonProjectLevelCondition = false;

        for (PolicyRuleExpressionView expression : getExpressionList()) {
            PolicyRuleConditionType condition = PolicyRuleConditionType.valueOf(expression.getName());
            if (PolicyRuleConditionType.UNKNOWN_RULE_CONDTION == condition) {
                continue;
            }
            if (PolicyRuleCategoryType.COMPONENT == condition.getPolicyRuleCategory()) {
                hasNonProjectLevelCondition = true;
            }
        }
        return !hasNonProjectLevelCondition;
    }

}
