package com.blackducksoftware.integration.hub.dataservices.items;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PolicyViolationItemTest {

	@Test
	public void contentItemConstructorTest() {
		final String projectName = "test project";
		final String projectVersion = "0.1.0";
		final String componentName = "component 1";
		final String componentVersion = "0.9.8";
		final List<String> policyNames = new ArrayList<>();
		policyNames.add("Policy 1");
		policyNames.add("Policy 2");

		final PolicyViolationContentItem item = new PolicyViolationContentItem(projectName, projectVersion,
				componentName, componentVersion, policyNames);

		assertEquals(projectName, item.getProjectName());
		assertEquals(projectVersion, item.getProjectVersion());
		assertEquals(componentName, item.getComponentName());
		assertEquals(componentVersion, item.getComponentVersion());
		assertEquals(policyNames, item.getPolicyNameList());
	}
}
