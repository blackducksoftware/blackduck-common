package com.blackducksoftware.integration.hub.dataservices.items;

import java.util.List;

public class PolicyViolationContentItem extends NotificationContentItem {

	private final List<String> policyNameList;

	public PolicyViolationContentItem(final String projectName, final String projectVersion, final String componentName,
			final String componentVersion, final List<String> policyNameList) {
		super(projectName, projectVersion, componentName, componentVersion);
		this.policyNameList = policyNameList;
	}

	public List<String> getPolicyNameList() {
		return policyNameList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((policyNameList == null) ? 0 : policyNameList.hashCode());
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
		final PolicyViolationContentItem other = (PolicyViolationContentItem) obj;
		if (policyNameList == null) {
			if (other.policyNameList != null) {
				return false;
			}
		} else if (!policyNameList.equals(other.policyNameList)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PolicyViolationContentItem [projectName=" + getProjectName() + ", projectVersion=" + getProjectVersion()
				+ ", componentName=" + getComponentName() + ", componentVersion=" + getComponentVersion()
				+ ", policyNameList=" + policyNameList + "]";
	}
}
