package com.blackducksoftware.integration.hub.dataservices.items;

import java.util.UUID;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationContentItem {
	private final ProjectVersion projectVersion;
	private final String componentName;
	private final String componentVersion;

	private final UUID componentId;
	private final UUID componentVersionId;

	public NotificationContentItem(final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId) {
		this.projectVersion = projectVersion;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
		this.componentId = componentId;
		this.componentVersionId = componentVersionId;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public UUID getComponentId() {
		return componentId;
	}

	public UUID getComponentVersionId() {
		return componentVersionId;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("NotificationContentItem [projectVersion=");
		builder.append(projectVersion);
		builder.append(", componentName=");
		builder.append(componentName);
		builder.append(", componentVersion=");
		builder.append(componentVersion);
		builder.append(", componentId=");
		builder.append(componentId);
		builder.append(", componentVersionId=");
		builder.append(componentVersionId);
		builder.append("]");
		return builder.toString();
	}

}
