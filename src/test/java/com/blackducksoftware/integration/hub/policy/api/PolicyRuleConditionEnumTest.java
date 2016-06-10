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

public class PolicyRuleConditionEnumTest {

	@Test
	public void testGetPolicyRuleConditionFieldEnum() {
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionFieldEnum("Fake"));
		assertEquals(PolicyRuleConditionFieldEnum.COMPONENT_USAGE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(
						PolicyRuleConditionFieldEnum.COMPONENT_USAGE.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionFieldEnum.HIGH_SEVERITY_VULN_COUNT, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.HIGH_SEVERITY_VULN_COUNT.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.LICENSE_FAMILY, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.LICENSE_FAMILY.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionFieldEnum.LOW_SEVERITY_VULN_COUNT, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.LOW_SEVERITY_VULN_COUNT.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.MEDIUM_SEVERITY_VULN_COUNT, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(
						PolicyRuleConditionFieldEnum.MEDIUM_SEVERITY_VULN_COUNT.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionFieldEnum.NEWER_VERSIONS_COUNT, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.NEWER_VERSIONS_COUNT.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.PROJECT_TIER, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.PROJECT_TIER.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionFieldEnum.SINGLE_LICENSE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.SINGLE_LICENSE.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.SINGLE_VERSION, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.SINGLE_VERSION.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.VERSION_DISTRIBUTION, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.VERSION_DISTRIBUTION.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.VERSION_PHASE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.VERSION_PHASE.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionFieldEnum(
						PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION.toString().toLowerCase()));
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionFieldEnum(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION.toString()));
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionFieldEnum(null));
	}

	@Test
	public void testGetPolicyRuleConditionByDisplayValue() {
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue("Fake"));
		assertEquals(PolicyRuleConditionFieldEnum.COMPONENT_USAGE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.COMPONENT_USAGE.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.HIGH_SEVERITY_VULN_COUNT,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.HIGH_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.LICENSE_FAMILY, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.LICENSE_FAMILY.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.LOW_SEVERITY_VULN_COUNT,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.LOW_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.MEDIUM_SEVERITY_VULN_COUNT,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.MEDIUM_SEVERITY_VULN_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.NEWER_VERSIONS_COUNT,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.NEWER_VERSIONS_COUNT.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.PROJECT_TIER, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.PROJECT_TIER.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.SINGLE_LICENSE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.SINGLE_LICENSE.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.SINGLE_VERSION, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.SINGLE_VERSION.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.VERSION_DISTRIBUTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.VERSION_DISTRIBUTION.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.VERSION_PHASE, PolicyRuleConditionFieldEnum
				.getPolicyRuleConditionByDisplayValue(PolicyRuleConditionFieldEnum.VERSION_PHASE.getDisplayValue()));
		assertEquals(PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION,
				PolicyRuleConditionFieldEnum.getPolicyRuleConditionByDisplayValue(
						PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION.getDisplayValue()));
	}
}
