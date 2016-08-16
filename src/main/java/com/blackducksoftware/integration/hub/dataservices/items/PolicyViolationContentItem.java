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
	public String toString() {
		return "PolicyViolationContentItem [projectName=" + getProjectName() + ", projectVersion=" + getProjectVersion()
				+ ", componentName=" + getComponentName() + ", componentVersion=" + getComponentVersion()
				+ ", policyNameList=" + policyNameList + "]";
	}
}
