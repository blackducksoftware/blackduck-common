package com.blackducksoftware.integration.hub.dataservices.items;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;

public abstract class AbstractItemCount {
	public abstract int getCount();

	public abstract int increment(final NotificationItem item);

	public abstract void reset();
}
