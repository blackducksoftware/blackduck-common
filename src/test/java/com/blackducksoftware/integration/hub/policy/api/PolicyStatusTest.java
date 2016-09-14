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
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class PolicyStatusTest {
	@Test
	public void testPolicyStatus() {
		final String overallStatus1 = PolicyStatusEnum.UNKNOWN.toString();
		final String updatedAt1 = "time1";
		final int value1 = 3214;
		final ComponentVersionStatusCount statusCount1 = new ComponentVersionStatusCount(PolicyStatusEnum.UNKNOWN,
				value1);
		final List<ComponentVersionStatusCount> counts1 = new ArrayList<>();
		counts1.add(statusCount1);
		final String allow1 = "allow1";
		final List<String> allows1 = new ArrayList<>();
		allows1.add(allow1);
		final String href1 = "href1";
		final MetaLink link1 = new MetaLink("rel1", "link1");
		final List<MetaLink> links1 = new ArrayList<>();
		links1.add(link1);
		final MetaInformation _meta1 = new MetaInformation(allows1, href1, links1);

		final String overallStatus2 = PolicyStatusEnum.IN_VIOLATION.toString();
		final String updatedAt2 = new DateTime().toString();
		final int value2 = 0;
		final ComponentVersionStatusCount statusCount2 = new ComponentVersionStatusCount(
				PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, value2);
		final List<ComponentVersionStatusCount> counts2 = new ArrayList<>();
		counts2.add(statusCount2);
		final String allow2 = "allow2";
		final List<String> allows2 = new ArrayList<>();
		allows2.add(allow2);
		final String href2 = "href2";
		final MetaLink link2 = new MetaLink("rel2", "link2");
		final List<MetaLink> links2 = new ArrayList<>();
		links2.add(link2);
		final MetaInformation _meta2 = new MetaInformation(allows2, href2, links2);

		final PolicyStatusItem item1 = new PolicyStatusItem(PolicyStatusEnum.UNKNOWN, updatedAt1, counts1, _meta1);
		final PolicyStatusItem item2 = new PolicyStatusItem(PolicyStatusEnum.IN_VIOLATION, updatedAt2, counts2, _meta2);
		final PolicyStatusItem item3 = new PolicyStatusItem(PolicyStatusEnum.UNKNOWN, updatedAt1, counts1, _meta1);
		final PolicyStatusItem item4 = new PolicyStatusItem(PolicyStatusEnum.UNKNOWN, null, null, null);

		assertEquals(overallStatus1, item1.getOverallStatus());
		assertEquals(PolicyStatusEnum.UNKNOWN, item1.getOverallStatus());
		assertEquals(updatedAt1, item1.getUpdatedAt());
		assertNull(item1.getUpdatedAtTime());
		assertEquals(counts1, item1.getComponentVersionStatusCounts());
		assertEquals(_meta1, item1.getMeta());

		assertEquals(overallStatus2, item2.getOverallStatus());
		assertEquals(PolicyStatusEnum.IN_VIOLATION, item2.getOverallStatus());
		assertEquals(updatedAt2, item2.getUpdatedAt());
		assertEquals(updatedAt2, item2.getUpdatedAtTime().toString());
		assertEquals(counts2, item2.getComponentVersionStatusCounts());
		assertEquals(_meta2, item2.getMeta());

		assertEquals(PolicyStatusEnum.UNKNOWN, item4.getOverallStatus());
		assertNull(item4.getUpdatedAtTime());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(PolicyStatusItem.class).suppress(Warning.STRICT_INHERITANCE).verify();

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
		PolicyStatusItem status = new PolicyStatusItem(null, null, null, null);

		assertNull(status.getCountInViolation());

		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.UNKNOWN, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountInViolation());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.NOT_IN_VIOLATION, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountInViolation());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.IN_VIOLATION, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertEquals(statusCount, status.getCountInViolation());
	}

	@Test
	public void testGetCountNotInViolation() {
		PolicyStatusItem status = new PolicyStatusItem(null, null, null, null);

		assertNull(status.getCountNotInViolation());

		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.UNKNOWN, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountNotInViolation());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.IN_VIOLATION, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountNotInViolation());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.NOT_IN_VIOLATION, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertEquals(statusCount, status.getCountNotInViolation());
	}

	@Test
	public void testGetCountInViolationOveridden() {
		PolicyStatusItem status = new PolicyStatusItem(null, null, null, null);

		assertNull(status.getCountInViolationOverridden());

		int value = 346;
		ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.UNKNOWN, value);
		List<ComponentVersionStatusCount> counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountInViolationOverridden());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.NOT_IN_VIOLATION, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertNull(status.getCountInViolationOverridden());

		value = 435;
		statusCount = new ComponentVersionStatusCount(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, value);
		counts = new ArrayList<>();
		counts.add(statusCount);

		status = new PolicyStatusItem(null, null, counts, null);

		assertEquals(statusCount, status.getCountInViolationOverridden());
	}

}
