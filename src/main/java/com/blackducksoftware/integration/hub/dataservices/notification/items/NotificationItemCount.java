package com.blackducksoftware.integration.hub.dataservices.notification.items;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;

public class NotificationItemCount extends AbstractItemCount {

	int count = 0;

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public int increment(final NotificationItem item) {
		return count++;
	}

	@Override
	public void reset() {
		count = 0;
	}
}
