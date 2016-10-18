/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyExpression;
import com.blackducksoftware.integration.hub.api.policy.PolicyRuleConditionEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyValue;

public class PolicyExpressionTest {

    @Test
    public void testPolicyExpression() {
        final String label1 = "label1";
        final String value1 = "value1";

        final String label2 = "label2";
        final String value2 = "value2";

        final PolicyValue policyValue1 = new PolicyValue(label1, value1);
        final PolicyValue policyValue2 = new PolicyValue(label2, value2);

        final List<PolicyValue> values1 = new ArrayList<>();
        values1.add(policyValue1);

        final List<PolicyValue> values2 = new ArrayList<>();
        values2.add(policyValue2);

        final String name1 = "name1";
        final String operation1 = "operation1";

        final String name2 = "name2";
        final String operation2 = "operation2";

        final PolicyExpression item1 = new PolicyExpression(name1, operation1, values1);
        final PolicyExpression item2 = new PolicyExpression(name2, operation2, values2);
        final PolicyExpression item3 = new PolicyExpression(name1, operation1, values1);

        assertEquals(name1, item1.getName());
        assertEquals(operation1, item1.getOperation());
        assertEquals(values1, item1.getValues());

        assertEquals(name2, item2.getName());
        assertEquals(operation2, item2.getOperation());
        assertEquals(values2, item2.getValues());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());
    }

    @Test
    public void testName() {
        String name = null;

        final PolicyExpression item1 = new PolicyExpression(name, null, null);

        assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION, item1.getNameConditionEnum());

        name = "";

        final PolicyExpression item2 = new PolicyExpression(name, null, null);

        assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION, item2.getNameConditionEnum());

        name = "gobbletygook";

        final PolicyExpression item3 = new PolicyExpression(name, null, null);

        assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION, item3.getNameConditionEnum());

        name = PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT.name();

        final PolicyExpression item4 = new PolicyExpression(name, null, null);

        assertEquals(PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT, item4.getNameConditionEnum());
    }

}
