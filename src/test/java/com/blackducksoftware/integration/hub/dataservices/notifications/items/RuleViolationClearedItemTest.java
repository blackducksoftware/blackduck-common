package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;

public class RuleViolationClearedItemTest {

	@Test
	public void contentItemConstructorTest() {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("test project");
		projectVersion.setProjectVersionName("0.1.0");
		final String componentName = "component 1";
		final String componentVersion = "0.9.8";
		final UUID componentId = UUID.randomUUID();
		final UUID componentVersionId = UUID.randomUUID();

		final List<PolicyRule> policyRules = new ArrayList<>();
		final PolicyRule policy1 = new PolicyRule(null, "Policy 1", null, null, null, null, null, null, null, null);
		final PolicyRule policy2 = new PolicyRule(null, "Policy 2", null, null, null, null, null, null, null, null);
		policyRules.add(policy1);
		policyRules.add(policy2);

		final PolicyViolationContentItem item = new PolicyViolationContentItem(new Date(), projectVersion,
				componentName,
				componentVersion, componentId, componentVersionId, policyRules);

		assertEquals(projectVersion, item.getProjectVersion());
		assertEquals(componentName, item.getComponentName());
		assertEquals(componentVersion, item.getComponentVersion());
		assertEquals(componentId, item.getComponentId());
		assertEquals(componentVersionId, item.getComponentVersionId());
		assertEquals(policyRules, item.getPolicyRuleList());
	}
}
