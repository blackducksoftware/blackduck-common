package com.blackducksoftware.integration.hub.dataservices.items;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PolicyOverrideItemTest {

	@Test
	public void contentItemConstructorTest() {
		final String projectName = "test project";
		final String projectVersion = "0.1.0";
		final String componentName = "component 1";
		final String componentVersion = "0.9.8";
		final String firstName = "myName";
		final String lastName = "noMyName";
		final List<String> policyNames = new ArrayList<>();
		policyNames.add("Policy 1");
		policyNames.add("Policy 2");

		final PolicyOverrideContentItem item = new PolicyOverrideContentItem(projectName, projectVersion, componentName,
				componentVersion, policyNames, firstName, lastName);

		assertEquals(projectName, item.getProjectName());
		assertEquals(projectVersion, item.getProjectVersion());
		assertEquals(componentName, item.getComponentName());
		assertEquals(componentVersion, item.getComponentVersion());
		assertEquals(firstName, item.getFirstName());
		assertEquals(lastName, item.getLastName());
		assertEquals(policyNames, item.getPolicyNameList());
	}
}
