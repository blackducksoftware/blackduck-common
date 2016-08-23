package com.blackducksoftware.integration.hub.dataservices.items;

import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class PolicyViolationContentItem extends NotificationContentItem {

	private final List<String> policyNameList;

	public PolicyViolationContentItem(final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<String> policyNameList) {
		super(projectVersion, componentName, componentVersion, componentId, componentVersionId);
		this.policyNameList = policyNameList;
	}

	public List<String> getPolicyNameList() {
		return policyNameList;
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
		builder.append(", policyNameList=");
		builder.append(policyNameList);
		builder.append("]");
		return builder.toString();
	}

}
