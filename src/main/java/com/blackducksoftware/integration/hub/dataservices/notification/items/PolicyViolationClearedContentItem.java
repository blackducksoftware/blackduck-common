package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class PolicyViolationClearedContentItem extends PolicyViolationContentItem {

	public PolicyViolationClearedContentItem(final Date createdAt, final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<PolicyRule> policyRuleList) {
		super(createdAt, projectVersion, componentName, componentVersion, componentId, componentVersionId,
				policyRuleList);
	}
}
