package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class PolicyViolationContentItem extends NotificationContentItem {

	private final List<PolicyRule> policyRuleList;

	public PolicyViolationContentItem(final Date createdAt, final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<PolicyRule> policyRuleList) {
		super(createdAt, projectVersion, componentName, componentVersion, componentId, componentVersionId);
		this.policyRuleList = policyRuleList;
	}

	public List<PolicyRule> getPolicyRuleList() {
		return policyRuleList;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyViolationContentItem [projectVersion=");
		builder.append(getProjectVersion());
		builder.append(", componentName=");
		builder.append(getComponentName());
		builder.append(", componentVersion=");
		builder.append(getComponentVersion());
		builder.append(", componentId=");
		builder.append(getComponentId());
		builder.append(", componentVersionId=");
		builder.append(getComponentVersionId());
		builder.append(", policyRuleList=");
		builder.append(policyRuleList);
		builder.append("]");
		return builder.toString();
	}

}
