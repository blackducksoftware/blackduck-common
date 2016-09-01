package com.blackducksoftware.integration.hub.dataservices.notification.transforms;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateBuilder;

public class NotificationCounter {
	private final Map<String, ProjectAggregateBuilder> countBuilderMap;

	public NotificationCounter(final Map<String, ProjectAggregateBuilder> countBuilderMap) {
		this.countBuilderMap = countBuilderMap;
	}

	public void count(final NotificationContentItem item) {
		final ProjectAggregateBuilder builder = getCountBuilder(item.getProjectVersion());
		builder.increment(item);
	}

	public Map<String, ProjectAggregateBuilder> getCountBuilderMap() {
		return countBuilderMap;
	}

	public ProjectAggregateBuilder getCountBuilder(final ProjectVersion projectVersion) {
		final String key = projectVersion.getProjectVersionLink();
		if (getCountBuilderMap().containsKey(key)) {
			return getCountBuilderMap().get(key);
		} else {
			ProjectAggregateBuilder builder = new ProjectAggregateBuilder();
			if (key != null) {
				builder = builder.updateProjectVersion(projectVersion);
				getCountBuilderMap().put(key, builder);
			}
			return builder;
		}
	}
}
