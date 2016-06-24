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

public enum PolicyRuleConditionFieldEnum {
	PROJECT_TIER("Project Tier"),
	VERSION_PHASE("Version Phase"),
	VERSION_DISTRIBUTION("Version Distribution"),
	SINGLE_VERSION("Component"),
	COMPONENT_USAGE("Component Usage"),
	LICENSE_FAMILY("License Family"),
	SINGLE_LICENSE("License"),
	NEWER_VERSIONS_COUNT("Newer Versions Count"),
	HIGH_SEVERITY_VULN_COUNT("High Severity Vulnerability Count"),
	MEDIUM_SEVERITY_VULN_COUNT("Medium Severity Vulnerability Count"),
	LOW_SEVERITY_VULN_COUNT("Low Severity Vulnerability Count"),
	UNKNOWN_RULE_CONDTION("Unknown Rule Condition");

	private final String displayValue;

	private PolicyRuleConditionFieldEnum(final String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public static PolicyRuleConditionFieldEnum getPolicyRuleConditionByDisplayValue(final String displayValue) {
		for (final PolicyRuleConditionFieldEnum currentEnum : PolicyRuleConditionFieldEnum.values()) {
			if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
				return currentEnum;
			}
		}
		return PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION;
	}

	public static PolicyRuleConditionFieldEnum getPolicyRuleConditionFieldEnum(final String distribution) {
		if (distribution == null) {
			return PolicyRuleConditionFieldEnum.UNKNOWN_RULE_CONDTION;
		}
		PolicyRuleConditionFieldEnum distributionEnum;
		try {
			distributionEnum = PolicyRuleConditionFieldEnum.valueOf(distribution.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			distributionEnum = UNKNOWN_RULE_CONDTION;
		}
		return distributionEnum;
	}
}
