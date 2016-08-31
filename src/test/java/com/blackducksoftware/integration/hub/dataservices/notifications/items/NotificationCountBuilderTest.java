package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationCountData;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;

public class NotificationCountBuilderTest {

	private static final String PROJECT_LINK = "aLink";
	private static final String PROJECT_VERSION = "0.0.1-TEST";
	private static final String PROJECT_NAME = "Project1";
	private static final String COMPONENT_NAME = "Component1";
	private static final String COMPONENT_VERSION = "Component-Version";
	private static final String POLICY_RULE_NAME = "PolicyRule";
	private static final String FIRST_NAME = "FirstName";
	private static final String LAST_NAME = "LastName";
	private static final String VULN_ID = "VulnId";
	private static final String VULN_SOURCE = "VulnSource";

	private ProjectVersion createProjectVersion() {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName(PROJECT_NAME);
		projectVersion.setProjectVersionName(PROJECT_VERSION);
		projectVersion.setProjectVersionLink(PROJECT_LINK);
		return projectVersion;
	}

	private PolicyViolationContentItem createPolicyViolationContentItem() {
		final UUID componentID = UUID.randomUUID();
		final UUID componentVersionID = UUID.randomUUID();

		final List<PolicyRule> policyRuleList = new ArrayList<>();
		final PolicyRule rule = new PolicyRule(null, POLICY_RULE_NAME, "", true, true, null, "", "", "", "");
		policyRuleList.add(rule);
		final PolicyViolationContentItem item = new PolicyViolationContentItem(createProjectVersion(), COMPONENT_NAME,
				COMPONENT_VERSION, componentID, componentVersionID, policyRuleList);
		return item;
	}

	private PolicyOverrideContentItem createPolicyOverrideContentItem() {
		final UUID componentID = UUID.randomUUID();
		final UUID componentVersionID = UUID.randomUUID();

		final List<PolicyRule> policyRuleList = new ArrayList<>();
		final PolicyRule rule = new PolicyRule(null, POLICY_RULE_NAME, "", true, true, null, "", "", "", "");
		policyRuleList.add(rule);
		final PolicyOverrideContentItem item = new PolicyOverrideContentItem(createProjectVersion(), COMPONENT_NAME,
				COMPONENT_VERSION, componentID, componentVersionID, policyRuleList, FIRST_NAME, LAST_NAME);
		return item;
	}

	private VulnerabilityContentItem createVulnerabilityContentItem() {
		final UUID componentID = UUID.randomUUID();
		final UUID componentVersionID = UUID.randomUUID();

		final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId(VULN_SOURCE, VULN_ID);
		final List<VulnerabilitySourceQualifiedId> added = new ArrayList<>();
		final List<VulnerabilitySourceQualifiedId> updated = new ArrayList<>();
		final List<VulnerabilitySourceQualifiedId> deleted = new ArrayList<>();
		added.add(vuln);
		updated.add(vuln);
		deleted.add(vuln);

		final VulnerabilityContentItem item = new VulnerabilityContentItem(createProjectVersion(), COMPONENT_NAME,
				COMPONENT_VERSION, componentID, componentVersionID, added, updated, deleted);
		return item;
	}

	private void updatePolicyViolationCounts(final NotificationCountBuilder builder, final int iterations) {
		final PolicyViolationContentItem item = createPolicyViolationContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
		}
	}

	private void updatePolicyOverrideCounts(final NotificationCountBuilder builder, final int iterations) {
		final PolicyOverrideContentItem item = createPolicyOverrideContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
		}
	}

	private void updateVulnerabilityCounts(final NotificationCountBuilder builder, final int iterations) {
		final VulnerabilityContentItem item = createVulnerabilityContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
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
		final PolicyViolationContentItem item = createPolicyViolationContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
		}
		final NotificationCountData data = builder.build();
		assertEquals(count, data.getPolicyViolationCount());
	}

	@Test
	public void testPolicyOverrideIncrement() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		final PolicyOverrideContentItem item = createPolicyOverrideContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
		}
		final NotificationCountData data = builder.build();
		assertEquals(count, data.getPolicyOverrideCount());
	}

	@Test
	public void testVulnerabilityIncrement() {
		final NotificationCountBuilder builder = new NotificationCountBuilder();
		final VulnerabilityContentItem item = createVulnerabilityContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
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
