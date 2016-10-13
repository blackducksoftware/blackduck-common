/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservices.notifications.items;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;

public class PolicyViolationItemTest {

	@Test
	public void contentItemConstructorTest() throws URISyntaxException {
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("test project");
		projectVersion.setProjectVersionName("0.1.0");
		final String componentName = "component 1";
		final String componentVersion = "0.9.8";
		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";

		final List<PolicyRule> policyRules = new ArrayList<>();
		final PolicyRule policy1 = new PolicyRule(null, "Policy 1", null, null, null, null, null, null, null, null);
		final PolicyRule policy2 = new PolicyRule(null, "Policy 2", null, null, null, null, null, null, null, null);
		policyRules.add(policy1);
		policyRules.add(policy2);

		final PolicyViolationContentItem item = new PolicyViolationContentItem(new Date(), projectVersion,
				componentName,
 componentVersion, null, componentVersionUrl, policyRules);

		assertEquals(projectVersion, item.getProjectVersion());
		assertEquals(componentName, item.getComponentName());
		assertEquals(componentVersion, item.getComponentVersion());
		assertEquals(componentVersionUrl, item.getComponentVersionUrl());
		assertEquals(policyRules, item.getPolicyRuleList());
	}
}
