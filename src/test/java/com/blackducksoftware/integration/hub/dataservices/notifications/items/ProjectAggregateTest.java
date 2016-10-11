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
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;

public class ProjectAggregateTest {

	@Test
	public void testProjectAggregateCount() throws URISyntaxException {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("Project Name");
		projectVersion.setProjectVersionName("1.0");
		projectVersion.setUrl("http://hub.bds.com/api/projects/1234/versions/5678");
		final String componentName = "componentName";
		final String componentVersion = "componentVersion";
		final String firstName = "firstName";
		final String lastName = "lastName";
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";
		final Date startDate = new Date();
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
		final ComponentAggregateData componentData = new ComponentAggregateData(componentName, componentVersion,
				violationList, overrideList, vulnerabilityList, vulnSize, vulnSize, vulnSize, vulnSummary);
		final List<ComponentAggregateData> componentList = new ArrayList<>();
		componentList.add(componentData);
		final Date endDate = new Date();

		final ProjectAggregateData data = new ProjectAggregateData(startDate, endDate, projectVersion,
				violationList.size(), overrideList.size(), vulnerabilityList.size(), total, vulnSize, vulnSize,
				vulnSize, componentList);
		assertNotNull(data);
		assertEquals(startDate, data.getStartDate());
		assertEquals(endDate, data.getEndDate());
		assertEquals(projectVersion, data.getProjectVersion());
		assertEquals(total, data.getTotal());
		assertEquals(violationList.size(), data.getPolicyViolationCount());
		assertEquals(overrideList.size(), data.getPolicyOverrideCount());
		assertEquals(vulnerabilityList.size(), data.getVulnerabilityCount());
		assertEquals(sourceIdList.size(), data.getVulnAddedCount());
		assertEquals(sourceIdList.size(), data.getVulnUpdatedCount());
		assertEquals(sourceIdList.size(), data.getVulnDeletedCount());
		assertEquals(violationList.size(), data.getPolicyViolationCount());
		assertEquals(overrideList.size(), data.getPolicyOverrideCount());
		assertEquals(vulnerabilityList.size(), data.getVulnerabilityCount());
		assertEquals(componentList, data.getComponentList());
	}
}
