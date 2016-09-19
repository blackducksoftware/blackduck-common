package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationContentItem implements Comparable<NotificationContentItem> {
	private final ProjectVersion projectVersion;
	private final String componentName;
	private final String componentVersion;

	private final UUID componentId;
	private final UUID componentVersionId;

	// We need createdAt (from the enclosing notificationItem) so we can order
	// them after
	// they are collected multi-threaded
	public final Date createdAt;

	public NotificationContentItem(final Date createdAt, final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId) {
		this.createdAt = createdAt;
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

	public Date getCreatedAt() {
		return createdAt;
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

	@Override
	public int compareTo(final NotificationContentItem o) {
		return getCreatedAt().compareTo(o.getCreatedAt());
	}

}
