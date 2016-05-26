/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.version.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ReleaseItemTest {

	@Test
	public void testReleaseItem() {
		final String versionName1 = "versionName1";
		final String phase1 = "phase1";
		final String distribution1 = "distribution1";
		final String source1 = "source1";
		final String href1 = "href1";
		final MetaInformation metaInfo1 = new MetaInformation(null, href1, null);

		final String versionName2 = "versionName2";
		final String phase2 = PhaseEnum.ARCHIVED.name();
		final String distribution2 = DistributionEnum.EXTERNAL.name();
		final String source2 = "source2";
		final String href2 = "href2";
		final MetaInformation metaInfo2 = new MetaInformation(null, href2, null);

		final ReleaseItem item1 = new ReleaseItem(versionName1, phase1, distribution1, source1, metaInfo1);
		final ReleaseItem item2 = new ReleaseItem(versionName2, phase2, distribution2, source2, metaInfo2);
		final ReleaseItem item3 = new ReleaseItem(versionName1, phase1, distribution1, source1, metaInfo1);

		assertEquals(versionName1, item1.getVersionName());
		assertEquals(phase1, item1.getPhase());
		assertEquals(distribution1, item1.getDistribution());
		assertEquals(source1, item1.getSource());
		assertEquals(href1, item1.get_meta().getHref());
		assertEquals(metaInfo1, item1.get_meta());

		assertEquals(versionName2, item2.getVersionName());
		assertEquals(phase2, item2.getPhase());
		assertEquals(distribution2, item2.getDistribution());
		assertEquals(PhaseEnum.ARCHIVED, item2.getPhaseEnum());
		assertEquals(DistributionEnum.EXTERNAL, item2.getDistributionEnum());
		assertEquals(source2, item2.getSource());
		assertEquals(href2, item2.get_meta().getHref());
		assertEquals(metaInfo2, item2.get_meta());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(ReleaseItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("ReleaseItem [versionName=");
		builder.append(item1.getVersionName());
		builder.append(", phase=");
		builder.append(item1.getPhase());
		builder.append(", distribution=");
		builder.append(item1.getDistribution());
		builder.append(", source=");
		builder.append(item1.getSource());
		builder.append(", _meta=");
		builder.append(item1.get_meta());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
