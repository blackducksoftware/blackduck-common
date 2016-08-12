package com.blackducksoftware.integration.hub.dataservices.transforms;

import java.util.List;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.notification.NotificationService;

public abstract class AbstractNotificationTransform {
	public final String KEY_PROJECT_NAME = "projectName";
	public final String KEY_PROJECT_VERSION = "projectVersionName";
	public final String KEY_COMPONENT_NAME = "componentName";
	public final String KEY_COMPONENT_VERSION = "componentVersionName";

	private final NotificationService notificationService;

	public AbstractNotificationTransform(final NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public abstract List<NotificationContentItem> transform(NotificationItem item) throws HubItemTransformException;

	public abstract String getNotificationType();
}
