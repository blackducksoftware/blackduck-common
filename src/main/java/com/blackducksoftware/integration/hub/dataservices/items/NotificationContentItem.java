package com.blackducksoftware.integration.hub.dataservices.items;

public class NotificationContentItem {
	private final String projectName;
	private final String projectVersion;
	private final String componentName;
	private final String componentVersion;

	public NotificationContentItem(final String projectName, final String projectVersion, final String componentName,
			final String componentVersion) {
		this.projectName = projectName;
		this.projectVersion = projectVersion;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	@Override
	public String toString() {
		return "NotificationContentItem [projectName=" + projectName + ", projectVersion=" + projectVersion
				+ ", componentName=" + componentName + ", componentVersion=" + componentVersion + "]";
	}
}
