package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.List;

public class PolicyNotificationFilter {
	private final List<String> ruleLinksToInclude;

	public PolicyNotificationFilter(final List<String> ruleLinksToInclude) {
		this.ruleLinksToInclude = ruleLinksToInclude;
	}

	public List<String> getRuleLinksToInclude() {
		return ruleLinksToInclude;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyFilter [ruleLinksToInclude=");
		builder.append(ruleLinksToInclude);
		builder.append("]");
		return builder.toString();
	}

}
