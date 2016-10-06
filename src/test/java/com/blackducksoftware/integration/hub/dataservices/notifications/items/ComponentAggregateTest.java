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
import com.blackducksoftware.integration.hub.dataservices.notification.items.ComponentAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ComponentVulnerabilitySummary;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;

public class ComponentAggregateTest {

	@Test
	public void testComponentAggregateCount() throws URISyntaxException {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("Project Name");
		projectVersion.setProjectVersionName("1.0");
		projectVersion.setUrl("versionLink");
		final String componentName = "componentName";
		final String componentVersion = "componentVersion";
		final String firstName = "firstName";
		final String lastName = "lastName";
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";
		final int total = 3;
		final List<PolicyViolationContentItem> violationList = new ArrayList<>();
		final List<PolicyOverrideContentItem> overrideList = new ArrayList<>();
		final List<VulnerabilityContentItem> vulnerabilityList = new ArrayList<>();
		final PolicyRule rule = new PolicyRule(null, "aRule", "", true, true, null, "", "", "", "");
		final List<PolicyRule> ruleList = new ArrayList<>();
		ruleList.add(rule);
		final PolicyViolationContentItem violationContent = new PolicyViolationContentItem(new Date(), projectVersion,
				componentName, componentVersion, componentVersionUrl, ruleList);
		final PolicyOverrideContentItem overrideContent = new PolicyOverrideContentItem(new Date(), projectVersion,
				componentName,
				componentVersion, componentVersionUrl, ruleList, firstName, lastName);

		final List<VulnerabilitySourceQualifiedId> sourceIdList = new ArrayList<>();
		sourceIdList.add(new VulnerabilitySourceQualifiedId("source", "id"));
		final VulnerabilityContentItem vulnerabilityContent = new VulnerabilityContentItem(new Date(), projectVersion,
				componentName, componentVersion, componentVersionUrl, sourceIdList, sourceIdList,
				sourceIdList);
		violationList.add(violationContent);
		overrideList.add(overrideContent);
		vulnerabilityList.add(vulnerabilityContent);
		final int vulnSize = sourceIdList.size();
		final ComponentVulnerabilitySummary vulnSummary = new ComponentVulnerabilitySummary(componentName,
				componentVersion, 1, 2, 3, 6);
		final ComponentAggregateData data = new ComponentAggregateData(componentName, componentVersion, violationList,
				overrideList, vulnerabilityList, vulnSize, vulnSize, vulnSize, vulnSummary);

		assertNotNull(data);
		assertEquals(total, data.getTotal());
		assertEquals(componentName, data.getComponentName());
		assertEquals(componentVersion, data.getComponentVersion());
		assertEquals(violationList.size(), data.getPolicyViolationCount());
		assertEquals(overrideList.size(), data.getPolicyOverrideCount());
		assertEquals(vulnerabilityList.size(), data.getVulnerabilityCount());
		assertEquals(sourceIdList.size(), data.getVulnAddedCount());
		assertEquals(sourceIdList.size(), data.getVulnUpdatedCount());
		assertEquals(sourceIdList.size(), data.getVulnDeletedCount());
		assertEquals(violationList, data.getPolicyViolationList());
		assertEquals(overrideList, data.getPolicyOverrideList());
		assertEquals(vulnerabilityList, data.getVulnerabilityList());
		assertEquals(vulnSummary, data.getVulnerabilitySummary());
	}
}
