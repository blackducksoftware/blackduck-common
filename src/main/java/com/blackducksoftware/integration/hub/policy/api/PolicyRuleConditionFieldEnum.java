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
