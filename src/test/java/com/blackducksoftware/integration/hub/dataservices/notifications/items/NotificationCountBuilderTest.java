package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountData;

public class NotificationCountBuilderTest {

	private static final String PROJECT_LINK = "aLink";
	private static final String PROJECT_VERSION = "0.0.1-TEST";
	private static final String PROJECT_NAME = "Project1";

	private VulnerabilityNotificationItem createVulnerability() {
		final VulnerabilityNotificationItem item = Mockito.mock(VulnerabilityNotificationItem.class);
		final VulnerabilityNotificationContent content = Mockito.mock(VulnerabilityNotificationContent.class);
		Mockito.when(item.getContent()).thenReturn(content);
		Mockito.when(content.getNewVulnerabilityCount()).thenReturn(1);
		Mockito.when(content.getUpdatedVulnerabilityCount()).thenReturn(1);
		Mockito.when(content.getDeletedVulnerabilityCount()).thenReturn(1);

		return item;
	}

	private ProjectVersion createProjectVersion() {
		final ProjectVersion projectVersion = Mockito.mock(ProjectVersion.class);
		Mockito.when(projectVersion.getProjectName()).thenReturn(PROJECT_NAME);
		Mockito.when(projectVersion.getProjectVersionName()).thenReturn(PROJECT_VERSION);
		Mockito.when(projectVersion.getProjectVersionLink()).thenReturn(PROJECT_LINK);
		return projectVersion;
	}

	private void updatePolicyViolationCounts(final NotificationCountBuilder builder, final int iterations) {
		final RuleViolationNotificationItem item = Mockito.mock(RuleViolationNotificationItem.class);
		for (int index = 0; index < iterations; index++) {
			builder.incrementPolicyCounts(item);
		}
	}

	private void updatePolicyOverrideCounts(final NotificationCountBuilder builder, final int iterations) {
		final PolicyOverrideNotificationItem item = Mockito.mock(PolicyOverrideNotificationItem.class);
		for (int index = 0; index < iterations; index++) {
			builder.incrementPolicyOverrideCounts(item);
		}
	}

	private void updateVulnerabilityCounts(final NotificationCountBuilder builder, final int iterations) {
		final VulnerabilityNotificationItem item = createVulnerability();
		for (int index = 0; index < iterations; index++) {
			builder.incrementVulnerabilityCounts(item);
		}
	}

	@Test
	public void testConstructor() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		assertNotNull(builder);
	}

	@Test
	public void testUpdateProjectVersion() {
		NotificationCountBuilder builder = new NotificationCountBuilder();
		builder = builder.updateProjectVersion(createProjectVersion());
		assertEquals(PROJECT_NAME, builder.getProjectVersion().getProjectName());
		assertEquals(PROJECT_VERSION, builder.getProjectVersion().getProjectVersionName());
		assertEquals(PROJECT_LINK, builder.getProjectVersion().getProjectVersionLink());
	}

	@Test
	public void testUpdateDateRange() {
		final long currentTime = System.currentTimeMillis();
		final Date start = new Date(currentTime - 10000);
		final Date end = new Date(currentTime);
		NotificationCountBuilder builder = new NotificationCountBuilder();
		builder = builder.updateDateRange(start, end);
		assertEquals(start, builder.getStartDate());
		assertEquals(end, builder.getEndDate());
	}

	@Test
	public void testPolicyViolationIncrement() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		final RuleViolationNotificationItem item = Mockito.mock(RuleViolationNotificationItem.class);
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.incrementPolicyCounts(item);
		}
		final NotificationCountData data = builder.build();
		assertEquals(count, data.getPolicyViolationCount());
	}

	@Test
	public void testPolicyOverrideIncrement() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		final PolicyOverrideNotificationItem item = Mockito.mock(PolicyOverrideNotificationItem.class);
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.incrementPolicyOverrideCounts(item);
		}
		final NotificationCountData data = builder.build();
		assertEquals(count, data.getPolicyOverrideCount());
	}

	@Test
	public void testVulnerabilityIncrement() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		final VulnerabilityNotificationItem item = createVulnerability();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.incrementVulnerabilityCounts(item);
		}
		final NotificationCountData data = builder.build();
		assertEquals(count, data.getVulnerabilityCount());
		assertEquals(count, data.getVulnAddedCount());
		assertEquals(count, data.getVulnUpdatedCount());
		assertEquals(count, data.getVulnDeletedCount());
	}

	@Test
	public void testFullObjectConstruction() {
		final long currentTime = System.currentTimeMillis();
		final Date start = new Date(currentTime - 10000);
		final Date end = new Date(currentTime);
		NotificationCountBuilder builder = new NotificationCountBuilder();
		builder = builder.updateDateRange(start, end);
		builder = builder.updateProjectVersion(createProjectVersion());

		final int policyCount = 2;
		final int policyOverrideCount = 3;
		final int vulnCount = 10;
		final int total = policyCount + policyOverrideCount + vulnCount;

		updatePolicyViolationCounts(builder, 2);
		updatePolicyOverrideCounts(builder, 3);
		updateVulnerabilityCounts(builder, 10);

		final NotificationCountData data = builder.build();
		assertEquals(start, builder.getStartDate());
		assertEquals(end, builder.getEndDate());
		assertEquals(PROJECT_NAME, builder.getProjectVersion().getProjectName());
		assertEquals(PROJECT_VERSION, builder.getProjectVersion().getProjectVersionName());
		assertEquals(PROJECT_LINK, builder.getProjectVersion().getProjectVersionLink());
		assertEquals(policyCount, data.getPolicyViolationCount());
		assertEquals(policyOverrideCount, data.getPolicyOverrideCount());
		assertEquals(vulnCount, data.getVulnerabilityCount());
		assertEquals(vulnCount, data.getVulnAddedCount());
		assertEquals(vulnCount, data.getVulnUpdatedCount());
		assertEquals(vulnCount, data.getVulnDeletedCount());
		assertEquals(total, data.getTotal());
	}
}
