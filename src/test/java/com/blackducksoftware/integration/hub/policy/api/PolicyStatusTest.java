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
package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.ComponentVersionStatusCount;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatus;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class PolicyStatusTest {

	@Test
	public void testPolicyStatus() {
		final String overallStatus1 = "status1";
		final String updatedAt1 = "time1";
		final String name1 = "name1";
		final int value1 = 3214;
		final ComponentVersionStatusCount statusCount1 = new ComponentVersionStatusCount(name1, value1);
		final List<ComponentVersionStatusCount> counts1 = new ArrayList<ComponentVersionStatusCount>();
		counts1.add(statusCount1);
		final String allow1 = "allow1";
		final List<String> allows1 = new ArrayList<String>();
		allows1.add(allow1);
		final String href1 = "href1";
		final MetaLink link1 = new MetaLink("rel1", "link1");
		final List<MetaLink> links1 = new ArrayList<MetaLink>();
		links1.add(link1);
		final MetaInformation _meta1 = new MetaInformation(allows1, href1, links1);

		final String overallStatus2 = PolicyStatusEnum.IN_VIOLATION.name();
		final String updatedAt2 = new DateTime().toString();
		final String name2 = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
		final int value2 = 0;
		final ComponentVersionStatusCount statusCount2 = new ComponentVersionStatusCount(name2, value2);
		final List<ComponentVersionStatusCount> counts2 = new ArrayList<ComponentVersionStatusCount>();
		counts2.add(statusCount2);
		final String allow2 = "allow2";
		final List<String> allows2 = new ArrayList<String>();
		allows2.add(allow2);
		final String href2 = "href2";
		final MetaLink link2 = new MetaLink("rel2", "link2");
		final List<MetaLink> links2 = new ArrayList<MetaLink>();
		links2.add(link2);
		final MetaInformation _meta2 = new MetaInformation(allows2, href2, links2);

		final PolicyStatus item1 = new PolicyStatus(overallStatus1, updatedAt1, counts1, _meta1);
		final PolicyStatus item2 = new PolicyStatus(overallStatus2, updatedAt2, counts2, _meta2);
		final PolicyStatus item3 = new PolicyStatus(overallStatus1, updatedAt1, counts1, _meta1);
		final PolicyStatus item4 = new PolicyStatus("", null, null, null);

		assertEquals(overallStatus1, item1.getOverallStatus());
		assertEquals(PolicyStatusEnum.UNKNOWN, item1.getOverallStatusEnum());
		assertEquals(updatedAt1, item1.getUpdatedAt());
		assertNull(item1.getUpdatedAtTime());
		assertEquals(counts1, item1.getComponentVersionStatusCounts());
		assertEquals(_meta1, item1.getMeta());

		assertEquals(overallStatus2, item2.getOverallStatus());
		assertEquals(PolicyStatusEnum.IN_VIOLATION, item2.getOverallStatusEnum());
		assertEquals(updatedAt2, item2.getUpdatedAt());
		assertEquals(updatedAt2, item2.getUpdatedAtTime().toString());
		assertEquals(counts2, item2.getComponentVersionStatusCounts());
		assertEquals(_meta2, item2.getMeta());

		assertEquals(PolicyStatusEnum.UNKNOWN, item4.getOverallStatusEnum());
		assertNull(item4.getUpdatedAtTime());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(PolicyStatus.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyStatus [overallStatus=");
		builder.append(item1.getOverallStatus());
		builder.append(", updatedAt=");
		builder.append(item1.getUpdatedAt());
		builder.append(", componentVersionStatusCounts=");
		builder.append(item1.getComponentVersionStatusCounts());
		builder.append(", meta=");
		builder.append(item1.getMeta());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

	@Test
	public void testGetCountInViolation() {
		PolicyStatus status = new PolicyStatus(null, null, null, null);

		assertNull(status.getCountInViolation());

		String name = "name";
		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountInViolation());

		name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountInViolation());

		name = PolicyStatusEnum.IN_VIOLATION.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertEquals(statusCount, status.getCountInViolation());

	}

	@Test
	public void testGetCountNotInViolation() {
		PolicyStatus status = new PolicyStatus(null, null, null, null);

		assertNull(status.getCountNotInViolation());

		String name = "name";
		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountNotInViolation());

		name = PolicyStatusEnum.IN_VIOLATION.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountNotInViolation());

		name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertEquals(statusCount, status.getCountNotInViolation());

	}

	@Test
	public void testGetCountInViolationOveridden() {
		PolicyStatus status = new PolicyStatus(null, null, null, null);

		assertNull(status.getCountInViolationOverridden());

		String name = "name";
		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountInViolationOverridden());

		name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertNull(status.getCountInViolationOverridden());

		name = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
		value = 435;
		statusCount = new ComponentVersionStatusCount(name, value);
		counts = new ArrayList<ComponentVersionStatusCount>();
		counts.add(statusCount);

		status = new PolicyStatus(null, null, counts, null);

		assertEquals(statusCount, status.getCountInViolationOverridden());

	}

}
