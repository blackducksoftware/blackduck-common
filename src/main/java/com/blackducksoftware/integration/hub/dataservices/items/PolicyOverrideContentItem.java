package com.blackducksoftware.integration.hub.dataservices.items;

import java.util.List;

public class PolicyOverrideContentItem extends PolicyViolationContentItem {

	private final String firstName;
	private final String lastName;

	public PolicyOverrideContentItem(final String projectName, final String projectVersion, final String componentName,
			final String componentVersion, final List<String> policyNameList, final String firstName,
			final String lastName) {
		super(projectName, projectVersion, componentName, componentVersion, policyNameList);
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
		return "PolicyOverrideContentItem [projectName=" + getProjectName() + ", projectVersion=" + getProjectVersion()
				+ ", componentName=" + getComponentName() + ", componentVersion=" + getComponentVersion()
				+ ", policyNameList=" + getPolicyNameList() + ", firstName=" + firstName + ", lastName=" + lastName
				+ "]";
	}
}
