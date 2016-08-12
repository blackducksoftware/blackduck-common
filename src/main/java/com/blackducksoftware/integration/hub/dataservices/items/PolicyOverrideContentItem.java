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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PolicyOverrideContentItem other = (PolicyOverrideContentItem) obj;
		if (firstName == null) {
			if (other.firstName != null) {
				return false;
			}
		} else if (!firstName.equals(other.firstName)) {
			return false;
		}
		if (lastName == null) {
			if (other.lastName != null) {
				return false;
			}
		} else if (!lastName.equals(other.lastName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PolicyOverrideContentItem [projectName=" + getProjectName() + ", projectVersion=" + getProjectVersion()
				+ ", componentName=" + getComponentName() + ", componentVersion=" + getComponentVersion()
				+ ", policyNameList=" + getPolicyNameList() + ", firstName=" + firstName + ", lastName=" + lastName
				+ "]";
	}
}
