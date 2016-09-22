package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;

public class NotificationContentItemTest {

	@Test
	public void testSorting() throws InterruptedException {
		final NotificationContentItem notif1 = new NotificationContentItem(new Date(), new ProjectVersion(),
				"componentName1", "componentVersion1", new UUID(0, 0), new UUID(0, 0));

		Thread.sleep(10L);

		final NotificationContentItem notif2 = new NotificationContentItem(new Date(), new ProjectVersion(),
				"componentName2", "componentVersion2", new UUID(0, 0), new UUID(0, 0));

		final Set<NotificationContentItem> items = new TreeSet<>();
		items.add(notif2);
		items.add(notif1);

		final Iterator<NotificationContentItem> iter = items.iterator();

		assertTrue(iter.hasNext());
		assertEquals("componentName1", iter.next().getComponentName());
		assertEquals("componentName2", iter.next().getComponentName());
	}

}
