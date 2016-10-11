package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ComponentAggregateBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ComponentAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;

public class ComponentAggregateBuilderTest {

	private static final String PROJECT_LINK = "http://hub.bds.com/api/projects/1234";
	private static final String PROJECT_VERSION = "0.0.1-TEST";
	private static final String PROJECT_NAME = "Project1";
	private static final String COMPONENT_NAME = "Component1";
	private static final String COMPONENT_VERSION = "Component-Version";
	private static final String POLICY_RULE_NAME = "PolicyRule";
	private static final String FIRST_NAME = "FirstName";
	private static final String LAST_NAME = "LastName";
	private static final String VULN_ID = "VulnId";
	private static final String VULN_SOURCE = "VulnSource";

	private ProjectVersion createProjectVersion() throws URISyntaxException {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName(PROJECT_NAME);
		projectVersion.setProjectVersionName(PROJECT_VERSION);
		projectVersion.setUrl(PROJECT_LINK);
		return projectVersion;
	}

	private PolicyViolationContentItem createPolicyViolationContentItem() throws URISyntaxException {
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";

		final List<PolicyRule> policyRuleList = new ArrayList<>();
		final PolicyRule rule = new PolicyRule(null, POLICY_RULE_NAME, "", true, true, null, "", "", "", "");
		policyRuleList.add(rule);
		final PolicyViolationContentItem item = new PolicyViolationContentItem(new Date(), createProjectVersion(),
				COMPONENT_NAME, COMPONENT_VERSION, componentVersionUrl, policyRuleList);
		return item;
	}

	private PolicyOverrideContentItem createPolicyOverrideContentItem() throws URISyntaxException {
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";

		final List<PolicyRule> policyRuleList = new ArrayList<>();
		final PolicyRule rule = new PolicyRule(null, POLICY_RULE_NAME, "", true, true, null, "", "", "", "");
		policyRuleList.add(rule);
		final PolicyOverrideContentItem item = new PolicyOverrideContentItem(new Date(), createProjectVersion(),
				COMPONENT_NAME, COMPONENT_VERSION, componentVersionUrl, policyRuleList, FIRST_NAME,
				LAST_NAME);
		return item;
	}

	private VulnerabilityContentItem createVulnerabilityContentItem() throws URISyntaxException {
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";

		final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId(VULN_SOURCE, VULN_ID);
		final List<VulnerabilitySourceQualifiedId> added = new ArrayList<>();
		final List<VulnerabilitySourceQualifiedId> updated = new ArrayList<>();
		final List<VulnerabilitySourceQualifiedId> deleted = new ArrayList<>();
		added.add(vuln);
		updated.add(vuln);
		deleted.add(vuln);

		final VulnerabilityContentItem item = new VulnerabilityContentItem(new Date(), createProjectVersion(),
				COMPONENT_NAME, COMPONENT_VERSION, componentVersionUrl, added, updated, deleted);
		return item;
	}

	private void updatePolicyViolationCounts(final ComponentAggregateBuilder builder, final int iterations)
			throws URISyntaxException {
		final PolicyViolationContentItem item = createPolicyViolationContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
		}
	}

	private void updatePolicyOverrideCounts(final ComponentAggregateBuilder builder, final int iterations)
			throws URISyntaxException {
		final PolicyOverrideContentItem item = createPolicyOverrideContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
		}
	}

	private void updateVulnerabilityCounts(final ComponentAggregateBuilder builder, final int iterations)
			throws URISyntaxException {
		final VulnerabilityContentItem item = createVulnerabilityContentItem();
		for (int index = 0; index < iterations; index++) {
			builder.increment(item);
		}
	}

	@Test
	public void testConstructor() {
		final ComponentAggregateBuilder builder = new ComponentAggregateBuilder();
		assertNotNull(builder);
	}

	@Test
	public void testPolicyViolationIncrement() throws URISyntaxException {
		final ComponentAggregateBuilder builder = new ComponentAggregateBuilder();
		final PolicyViolationContentItem item = createPolicyViolationContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
		}
		final ComponentAggregateData data = builder.build();
		assertEquals(count, data.getPolicyViolationCount());
	}

	@Test
	public void testPolicyOverrideIncrement() throws URISyntaxException {
		final ComponentAggregateBuilder builder = new ComponentAggregateBuilder();
		final PolicyOverrideContentItem item = createPolicyOverrideContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
		}
		final ComponentAggregateData data = builder.build();
		assertEquals(count, data.getPolicyOverrideCount());
	}

	@Test
	public void testVulnerabilityIncrement() throws URISyntaxException {
		final ComponentAggregateBuilder builder = new ComponentAggregateBuilder();
		final VulnerabilityContentItem item = createVulnerabilityContentItem();
		final int count = 5;
		for (int index = 0; index < count; index++) {
			builder.increment(item);
		}
		final ComponentAggregateData data = builder.build();
		assertEquals(count, data.getVulnerabilityCount());
		assertEquals(count, data.getVulnAddedCount());
		assertEquals(count, data.getVulnUpdatedCount());
		assertEquals(count, data.getVulnDeletedCount());
	}

	@Test
	public void testFullObjectConstruction() throws URISyntaxException {
		final ComponentAggregateBuilder builder = new ComponentAggregateBuilder();

		final int policyCount = 2;
		final int policyOverrideCount = 3;
		final int vulnCount = 10;
		final int total = policyCount + policyOverrideCount + vulnCount;

		updatePolicyViolationCounts(builder, 2);
		updatePolicyOverrideCounts(builder, 3);
		updateVulnerabilityCounts(builder, 10);

		final ComponentAggregateData data = builder.build();
		assertEquals(policyCount, data.getPolicyViolationCount());
		assertEquals(policyOverrideCount, data.getPolicyOverrideCount());
		assertEquals(vulnCount, data.getVulnerabilityCount());
		assertEquals(vulnCount, data.getVulnAddedCount());
		assertEquals(vulnCount, data.getVulnUpdatedCount());
		assertEquals(vulnCount, data.getVulnDeletedCount());
		assertEquals(total, data.getTotal());
	}
}
