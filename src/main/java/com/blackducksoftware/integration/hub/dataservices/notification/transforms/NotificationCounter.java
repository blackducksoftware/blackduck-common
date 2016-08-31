package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;

public class NotificationCounter {
	private final Map<String, NotificationCountBuilder> countBuilderMap;

	public NotificationCounter(final Map<String, NotificationCountBuilder> countBuilderMap) {
		this.countBuilderMap = countBuilderMap;
	}

	public void count(final NotificationContentItem item) {
		final NotificationCountBuilder builder = getCountBuilder(item.getProjectVersion());
		builder.increment(item);
	}

	public Map<String, NotificationCountBuilder> getCountBuilderMap() {
		return countBuilderMap;
	}

	public NotificationCountBuilder getCountBuilder(final ProjectVersion projectVersion) {
		final String key = projectVersion.getProjectVersionLink();
		if (getCountBuilderMap().containsKey(key)) {
			return getCountBuilderMap().get(key);
		} else {
			NotificationCountBuilder builder = new NotificationCountBuilder();
			if (key != null) {
				builder = builder.updateProjectVersion(projectVersion);
				getCountBuilderMap().put(key, builder);
			}
			return builder;
		}
	}
}
