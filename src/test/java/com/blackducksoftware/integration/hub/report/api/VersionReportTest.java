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
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.DetailedReleaseSummary;
import com.blackducksoftware.integration.hub.api.report.ProjectData;
import com.blackducksoftware.integration.hub.api.report.ReleaseData;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.report.DetailedReleaseSummary.URLProvider;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class VersionReportTest {

	@Test
	public void testVersionReport() {

		final AggregateBomViewEntry bomEntry1 = new AggregateBomViewEntry(null, null, null, null, null, null, null,
				null, null, null, null, null, null, null);
		final AggregateBomViewEntry bomEntry2 = new AggregateBomViewEntry(null, null, null, null, null, null, null,
				null, "notEqual", null, null, null, null, null);

		final List<AggregateBomViewEntry> aggregateBomViewEntries1 = new ArrayList<AggregateBomViewEntry>();
		aggregateBomViewEntries1.add(bomEntry1);
		final List<AggregateBomViewEntry> aggregateBomViewEntries2 = new ArrayList<AggregateBomViewEntry>();
		aggregateBomViewEntries2.add(bomEntry2);

		final DetailedReleaseSummary releaseSumary1 = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		final DetailedReleaseSummary releaseSumary2 = new DetailedReleaseSummary(null, null, null, null, null, "notEqual", null, null, null, null);

		final VersionReport item1 = new VersionReport(releaseSumary1, aggregateBomViewEntries1);
		final VersionReport item2 = new VersionReport(releaseSumary2, aggregateBomViewEntries2);
		final VersionReport item3 = new VersionReport(releaseSumary1, aggregateBomViewEntries1);

		assertEquals(aggregateBomViewEntries1, item1.getAggregateBomViewEntries());
		assertEquals(releaseSumary1, item1.getDetailedReleaseSummary());

		assertEquals(aggregateBomViewEntries2, item2.getAggregateBomViewEntries());
		assertEquals(releaseSumary2, item2.getDetailedReleaseSummary());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(VersionReport.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("VersionReport [detailedReleaseSummary=");
		builder.append(item1.getDetailedReleaseSummary());
		builder.append(", aggregateBomViewEntries=");
		builder.append(item1.getAggregateBomViewEntries());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

	@Test
	public void testGetBaseUrl() {
		final String baseUrl = "baseurl";
		URLProvider uiUrlGenerator = new URLProvider(baseUrl);
		DetailedReleaseSummary releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		VersionReport item = new VersionReport(releaseSumary, null);

		assertEquals(baseUrl, item.getBaseUrl());

		uiUrlGenerator = new URLProvider(null);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getBaseUrl());

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getBaseUrl());

		item = new VersionReport(null, null);

		assertNull(item.getBaseUrl());
	}

	@Test
	public void testGetReportProjectUrl() {
		final String baseUrl = "http://test";
		URLProvider uiUrlGenerator = new URLProvider(baseUrl);
		final String projectId = "projectId";
		DetailedReleaseSummary releaseSumary = new DetailedReleaseSummary(projectId, null, null, null, null, null, null, null, null, uiUrlGenerator);
		VersionReport item = new VersionReport(releaseSumary, null);

		assertTrue(StringUtils.isNotBlank(item.getReportProjectUrl()));
		assertEquals("http://test#projects/id:projectId", item.getReportProjectUrl());

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportProjectUrl());

		uiUrlGenerator = new URLProvider(null);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportProjectUrl());

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportProjectUrl());

		item = new VersionReport(null, null);

		assertNull(item.getReportProjectUrl());
	}

	@Test
	public void testGetReportVersionUrl() {
		final String baseUrl = "http://test";
		URLProvider uiUrlGenerator = new URLProvider(baseUrl);
		final String versionId = "versionId";
		DetailedReleaseSummary releaseSumary = new DetailedReleaseSummary(null, versionId, null, null, null, null, null, null, null, uiUrlGenerator);
		VersionReport item = new VersionReport(releaseSumary, null);

		assertTrue(StringUtils.isNotBlank(item.getReportVersionUrl()));
		assertEquals("http://test#versions/id:versionId/view:bom", item.getReportVersionUrl());

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportVersionUrl());

		uiUrlGenerator = new URLProvider(null);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportVersionUrl());

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		item = new VersionReport(releaseSumary, null);

		assertNull(item.getReportVersionUrl());

		item = new VersionReport(null, null);

		assertNull(item.getReportVersionUrl());
	}

	@Test
	public void testGetComponentUrl() {
		String projectId = "projectId";
		ProjectData project = new ProjectData(projectId, null, null);
		AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null,
				project, null, null, null, null);
		final String baseUrl = "http://test";
		URLProvider uiUrlGenerator = new URLProvider(baseUrl);
		DetailedReleaseSummary releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		VersionReport item = new VersionReport(releaseSumary, null);
		// Test valid
		assertTrue(StringUtils.isNotBlank(item.getComponentUrl(bomEntry)));

		projectId = "";
		project = new ProjectData(projectId, null, null);
		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, project, null, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// Empty project Id
		assertNull(item.getComponentUrl(bomEntry));

		project = new ProjectData(null, null, null);
		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, project, null, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// Null project id
		assertNull(item.getComponentUrl(bomEntry));

		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null project
		assertNull(item.getComponentUrl(bomEntry));

		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null entry
		assertNull(item.getComponentUrl(null));

		uiUrlGenerator = new URLProvider("");
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// empty base url
		assertNull(item.getComponentUrl(null));

		uiUrlGenerator = new URLProvider(null);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null base url
		assertNull(item.getComponentUrl(null));

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		item = new VersionReport(releaseSumary, null);
		// null url provider
		assertNull(item.getComponentUrl(null));

		item = new VersionReport(null, null);
		// null release summary
		assertNull(item.getComponentUrl(null));

	}

	@Test
	public void testGetVersionUrl() {
		String versionId = "versionid";
		ReleaseData release = new ReleaseData(versionId, null);
		List<ReleaseData> releases = new ArrayList<ReleaseData>();
		releases.add(release);
		AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null,
				null, releases, null, null, null);
		final String baseUrl = "http://test";
		URLProvider uiUrlGenerator = new URLProvider(baseUrl);
		DetailedReleaseSummary releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		VersionReport item = new VersionReport(releaseSumary, null);
		// Test valid
		assertTrue(StringUtils.isNotBlank(item.getVersionUrl(bomEntry)));

		versionId = "";
		release = new ReleaseData(versionId, null);
		releases = new ArrayList<ReleaseData>();
		releases.add(release);
		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, releases, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// Empty version Id
		assertNull(item.getVersionUrl(bomEntry));

		release = new ReleaseData(null, null);
		releases = new ArrayList<ReleaseData>();
		releases.add(release);
		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, releases, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// Null version id
		assertNull(item.getVersionUrl(bomEntry));

		releases = new ArrayList<ReleaseData>();
		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, releases, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// Empty releases
		assertNull(item.getVersionUrl(bomEntry));

		bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null,
				null, null);
		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null versions
		assertNull(item.getVersionUrl(bomEntry));

		uiUrlGenerator = new URLProvider(baseUrl);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null entry
		assertNull(item.getVersionUrl(null));

		uiUrlGenerator = new URLProvider("");
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// empty base url
		assertNull(item.getVersionUrl(null));

		uiUrlGenerator = new URLProvider(null);
		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, uiUrlGenerator);
		item = new VersionReport(releaseSumary, null);
		// null base url
		assertNull(item.getVersionUrl(null));

		releaseSumary = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);
		item = new VersionReport(releaseSumary, null);
		// null url provider
		assertNull(item.getVersionUrl(null));

		item = new VersionReport(null, null);
		// null release summary
		assertNull(item.getVersionUrl(null));

	}
}
