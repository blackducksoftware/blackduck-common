package com.blackducksoftware.integration.hub.dataservices.items;

import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class PolicyOverrideContentItem extends PolicyViolationContentItem {

	private final String firstName;
	private final String lastName;

	public PolicyOverrideContentItem(final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId,
			final List<String> policyNameList, final String firstName,
			final String lastName) {
		super(projectVersion, componentName, componentVersion, componentId, componentVersionId, policyNameList);
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyOverrideContentItem [projectVersion=");
		builder.append(getProjectVersion());
		builder.append(", componentName=");
		builder.append(getComponentName());
		builder.append(", componentVersion=");
		builder.append(getComponentVersion());
		builder.append(", componentId=");
		builder.append(getComponentId());
		builder.append(", componentVersionId=");
		builder.append(getComponentVersionId());
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append("]");
		return builder.toString();
	}

}
