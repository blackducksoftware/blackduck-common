package com.blackducksoftware.integration.hub.dataservices.items;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class PolicyViolationItemTest {

	@Test
	public void contentItemConstructorTest() {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("test project");
		projectVersion.setProjectVersionName("0.1.0");
		final String componentName = "component 1";
		final String componentVersion = "0.9.8";
		final UUID componentId = UUID.randomUUID();
		final UUID componentVersionId = UUID.randomUUID();

		final List<String> policyNames = new ArrayList<>();
		policyNames.add("Policy 1");
		policyNames.add("Policy 2");

		final PolicyViolationContentItem item = new PolicyViolationContentItem(projectVersion, componentName,
				componentVersion, componentId, componentVersionId, policyNames);

		assertEquals(projectVersion, item.getProjectVersion());
		assertEquals(componentName, item.getComponentName());
		assertEquals(componentVersion, item.getComponentVersion());
		assertEquals(componentId, item.getComponentId());
		assertEquals(componentVersionId, item.getComponentVersionId());
		assertEquals(policyNames, item.getPolicyNameList());
	}
}
