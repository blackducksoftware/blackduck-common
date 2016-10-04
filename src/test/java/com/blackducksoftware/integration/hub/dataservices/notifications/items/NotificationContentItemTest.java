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
	public void testSortingInOrder() throws InterruptedException {
		final NotificationContentItem notif1 = new NotificationContentItem(new Date(), new ProjectVersion(),
				"componentName1", "componentVersion1", new UUID(0, 0), new UUID(0, 0));

		Thread.sleep(10L);

		final NotificationContentItem notif2 = new NotificationContentItem(new Date(), new ProjectVersion(),
				"componentName2", "componentVersion2", new UUID(0, 0), new UUID(0, 0));

		final Set<NotificationContentItem> items = new TreeSet<>();
		items.add(notif1);
		items.add(notif2);

		final Iterator<NotificationContentItem> iter = items.iterator();

		assertTrue(iter.hasNext());
		assertEquals("componentName1", iter.next().getComponentName());
		assertEquals("componentName2", iter.next().getComponentName());
	}

	@Test
	public void testSortingReverseOrder() throws InterruptedException {
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

	@Test
	public void testSymmetry() throws InterruptedException {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("projectName");
		projectVersion.setProjectVersionName("projectVersionName");

		final Date createdAt = new Date();
		final NotificationContentItem notif1 = new NotificationContentItem(createdAt, projectVersion,
				"componentName1", "componentVersion1", new UUID(0, 0), new UUID(1, 1));

		final NotificationContentItem notif2 = new NotificationContentItem(createdAt, projectVersion,
				"componentName2", "componentVersion2", new UUID(2, 2), new UUID(3, 3));

		final int notif1CompareToResult = notif1.compareTo(notif2);
		final int notif2CompareToResult = notif2.compareTo(notif1);
		System.out.println("notif1CompareToResult: " + notif1CompareToResult);
		System.out.println("notif2CompareToResult: " + notif2CompareToResult);

		assertTrue(notif1CompareToResult == (-1 * notif2CompareToResult));
	}

}
