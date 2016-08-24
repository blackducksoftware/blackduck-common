package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.dataservices.notifications.items.NotificationItemCount;

public class NotificationItemCountTest {

	@Test
	public void testConstructor() {
		assertNotNull(new NotificationItemCount());
	}

	@Test
	public void testIncrement() {

		final NotificationItem item = Mockito.mock(NotificationItem.class);
		final NotificationItemCount counter = new NotificationItemCount();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			counter.increment(item);
		}

		assertEquals(count, counter.getCount());
	}

	@Test
	public void testReset() {
		final NotificationItem item = Mockito.mock(NotificationItem.class);
		final NotificationItemCount counter = new NotificationItemCount();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			counter.increment(item);
		}

		assertEquals(count, counter.getCount());
		counter.reset();
		assertEquals(0, counter.getCount());
	}
}
