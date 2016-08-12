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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
		result = prime * result + ((componentVersion == null) ? 0 : componentVersion.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + ((projectVersion == null) ? 0 : projectVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NotificationContentItem other = (NotificationContentItem) obj;
		if (componentName == null) {
			if (other.componentName != null) {
				return false;
			}
		} else if (!componentName.equals(other.componentName)) {
			return false;
		}
		if (componentVersion == null) {
			if (other.componentVersion != null) {
				return false;
			}
		} else if (!componentVersion.equals(other.componentVersion)) {
			return false;
		}
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (projectVersion == null) {
			if (other.projectVersion != null) {
				return false;
			}
		} else if (!projectVersion.equals(other.projectVersion)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "NotificationContentItem [projectName=" + projectName + ", projectVersion=" + projectVersion
				+ ", componentName=" + componentName + ", componentVersion=" + componentVersion + "]";
	}
}
