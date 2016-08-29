package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountData;

public class NotificationCountDataTest {

	@Test
	public void testNotificationItemCount() {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("Project Name");
		projectVersion.setProjectVersionName("1.0");
		projectVersion.setProjectVersionLink("versionLink");
		final Date startDate = new Date();
		final int total = 30;
		final int policyViolationCount = 1;
		final int policyOverrideCount = 2;
		final int vulnCount = 3;
		final int vulnAddedCount = 4;
		final int vulnUpdatedCount = 5;
		final int vulnDeletedCount = 6;
		final Date endDate = new Date();
		final NotificationCountData data = new NotificationCountData(startDate, endDate, projectVersion, total,
				policyViolationCount, policyOverrideCount, vulnCount, vulnAddedCount, vulnUpdatedCount,
				vulnDeletedCount);
		assertNotNull(data);
		assertEquals(startDate, data.getStartDate());
		assertEquals(endDate, data.getEndDate());
		assertEquals(projectVersion, data.getProjectVersion());
		assertEquals(total, data.getTotal());
		assertEquals(policyViolationCount, data.getPolicyViolationCount());
		assertEquals(policyOverrideCount, data.getPolicyOverrideCount());
		assertEquals(vulnCount, data.getVulnerabilityCount());
		assertEquals(vulnAddedCount, data.getVulnAddedCount());
		assertEquals(vulnUpdatedCount, data.getVulnUpdatedCount());
		assertEquals(vulnDeletedCount, data.getVulnDeletedCount());
	}
}
