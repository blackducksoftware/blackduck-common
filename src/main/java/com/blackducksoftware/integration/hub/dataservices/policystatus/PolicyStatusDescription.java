package com.blackducksoftware.integration.hub.dataservices.policystatus;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;

public class PolicyStatusDescription {
	private final PolicyStatusItem policyStatusItem;

	public PolicyStatusDescription(final PolicyStatusItem policyStatusItem) {
		this.policyStatusItem = policyStatusItem;
	}

	public String getPolicyStatusMessage() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("The Hub found: ");
		stringBuilder.append(policyStatusItem.getCountInViolation().getValue());
		stringBuilder.append(" components in violation, ");
		stringBuilder.append(policyStatusItem.getCountInViolationOverridden().getValue());
		stringBuilder.append(" components in violation, but overridden, and ");
		stringBuilder.append(policyStatusItem.getCountNotInViolation().getValue());
		stringBuilder.append(" components not in violation.");
		return stringBuilder.toString();
	}

}
