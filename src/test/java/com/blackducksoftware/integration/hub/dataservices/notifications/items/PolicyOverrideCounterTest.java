package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountData;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyOverrideCounter;

public class PolicyOverrideCounterTest {

	private static final String PROJECT_VERSION_LINK = "versionLink";
	private static final String PROJECT_VERSION = "0.0.1";
	private static final String PROJECT_NAME = "Project-1";
	private final Map<String, NotificationCountBuilder> countBuilderMap = new HashMap<>();

	private PolicyOverrideCounter createCounter() throws Exception {
		final ProjectVersionRestService projectVersionService = Mockito.mock(ProjectVersionRestService.class);
		final ReleaseItem releaseItem = Mockito.mock(ReleaseItem.class);
		Mockito.when(releaseItem.getVersionName()).thenReturn(PROJECT_VERSION);
		Mockito.when(projectVersionService.getProjectVersionReleaseItem(Mockito.anyString())).thenReturn(releaseItem);
		return new PolicyOverrideCounter(projectVersionService, countBuilderMap);
	}

	@Test
	public void testPolicyOverrideCounterConstructor() throws Exception {
		assertNotNull(createCounter());
	}

	@Test
	public void testPolicyOverrideCount() throws Exception {
		final PolicyOverrideCounter counter = createCounter();
		final PolicyOverrideNotificationItem item = Mockito.mock(PolicyOverrideNotificationItem.class);
		final PolicyOverrideNotificationContent content = Mockito.mock(PolicyOverrideNotificationContent.class);
		Mockito.when(content.getProjectName()).thenReturn(PROJECT_NAME);
		Mockito.when(content.getProjectVersionLink()).thenReturn(PROJECT_VERSION_LINK);
		Mockito.when(item.getContent()).thenReturn(content);

		final int count = 5;
		for (int index = 0; index < count; index++) {
			counter.count(item);
		}

		for (final Map.Entry<String, NotificationCountBuilder> entry : countBuilderMap.entrySet()) {
			final NotificationCountData data = entry.getValue().build();
			assertEquals(PROJECT_VERSION_LINK, entry.getKey());
			assertEquals(PROJECT_NAME, data.getProjectVersion().getProjectName());
			assertEquals(PROJECT_VERSION, data.getProjectVersion().getProjectVersionName());
			assertEquals(PROJECT_VERSION_LINK, data.getProjectVersion().getProjectVersionLink());
			assertEquals(count, data.getPolicyOverrideCount());
		}
	}
}
