package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountDataBuilder;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public abstract class AbstractNotificationCounter {
	private final ProjectVersionRestService projectVersionService;
	private final Map<String, NotificationCountDataBuilder> countBuilderMap;

	public AbstractNotificationCounter(final ProjectVersionRestService projectVersionService,
			final Map<String, NotificationCountDataBuilder> countBuilderMap) {
		this.projectVersionService = projectVersionService;
		this.countBuilderMap = countBuilderMap;
	}

	public ProjectVersionRestService getProjectVersionService() {
		return projectVersionService;
	}

	public abstract void count(NotificationItem item) throws HubItemTransformException;

	public Map<String, NotificationCountDataBuilder> getCountBuilderMap() {
		return countBuilderMap;
	}

	public NotificationCountDataBuilder getCountBuilder(final ProjectVersion projectVersion) {
		final String key = projectVersion.getProjectVersionLink();
		if (getCountBuilderMap().containsKey(key)) {
			return getCountBuilderMap().get(key);
		} else {
			NotificationCountDataBuilder builder = new NotificationCountDataBuilder();
			if (projectVersion.getProjectVersionLink() != null) {
				builder = builder.updateProjectVersion(projectVersion);
			}
			return builder;
		}
	}
}
