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

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyRuleConditionEnum;

public class PolicyRuleConditionEnumTest {

	@Test
	public void testGetPolicyRuleConditionFieldEnum() {
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionEnum.getPolicyRuleConditionFieldEnum("Fake"));
		assertEquals(PolicyRuleConditionEnum.COMPONENT_USAGE, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.COMPONENT_USAGE.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionEnum.HIGH_SEVERITY_VULN_COUNT, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.HIGH_SEVERITY_VULN_COUNT.toString()));
		assertEquals(PolicyRuleConditionEnum.LICENSE_FAMILY, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.LICENSE_FAMILY.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionEnum.LOW_SEVERITY_VULN_COUNT, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.LOW_SEVERITY_VULN_COUNT.toString()));
		assertEquals(PolicyRuleConditionEnum.MEDIUM_SEVERITY_VULN_COUNT,
				PolicyRuleConditionEnum.getPolicyRuleConditionFieldEnum(
						PolicyRuleConditionEnum.MEDIUM_SEVERITY_VULN_COUNT.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT.toString()));
		assertEquals(PolicyRuleConditionEnum.PROJECT_TIER, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.PROJECT_TIER.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionEnum.SINGLE_LICENSE, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.SINGLE_LICENSE.toString()));
		assertEquals(PolicyRuleConditionEnum.SINGLE_VERSION, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.SINGLE_VERSION.toString()));
		assertEquals(PolicyRuleConditionEnum.VERSION_DISTRIBUTION, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.VERSION_DISTRIBUTION.toString()));
		assertEquals(PolicyRuleConditionEnum.VERSION_PHASE, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.VERSION_PHASE.toString()));
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionEnum.getPolicyRuleConditionFieldEnum(
						PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION, PolicyRuleConditionEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION.toString()));
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionEnum.getPolicyRuleConditionFieldEnum(null));
	}

	@Test
	public void testGetPolicyRuleConditionByDisplayValue() {
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionEnum.getPolicyRuleConditionByDisplayValue("Fake"));
		assertEquals(PolicyRuleConditionEnum.COMPONENT_USAGE, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.COMPONENT_USAGE.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.HIGH_SEVERITY_VULN_COUNT,
				PolicyRuleConditionEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionEnum.HIGH_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.LICENSE_FAMILY, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.LICENSE_FAMILY.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.LOW_SEVERITY_VULN_COUNT,
				PolicyRuleConditionEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionEnum.LOW_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.MEDIUM_SEVERITY_VULN_COUNT,
				PolicyRuleConditionEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionEnum.MEDIUM_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.NEWER_VERSIONS_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.PROJECT_TIER, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.PROJECT_TIER.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.SINGLE_LICENSE, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.SINGLE_LICENSE.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.SINGLE_VERSION, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.SINGLE_VERSION.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.VERSION_DISTRIBUTION, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.VERSION_DISTRIBUTION.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.VERSION_PHASE, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.VERSION_PHASE.getDisplayValue()));
		assertEquals(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION, PolicyRuleConditionEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionEnum.UNKNOWN_RULE_CONDTION.getDisplayValue()));
	}
}
