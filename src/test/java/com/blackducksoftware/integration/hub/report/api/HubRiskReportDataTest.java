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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.report.AggregateBomViewEntry;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.VersionReport;
import com.blackducksoftware.integration.hub.api.report.risk.RiskCategories;
import com.blackducksoftware.integration.hub.api.report.risk.RiskCounts;
import com.blackducksoftware.integration.hub.api.report.risk.RiskProfile;
import com.blackducksoftware.integration.hub.util.XStreamHelperTest;
import com.blackducksoftware.integration.test.TestUtils;

public class HubRiskReportDataTest {
	@Test
	public void testSetReportHighRisks() {
		final RiskCounts counts = new RiskCounts(1, 0, 0, 0, 0);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(1, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(1, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(1, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskNoneCount());
	}

	@Test
	public void testSetReportSecurityHighRisks() {
		final RiskCounts counts = new RiskCounts(1, 1, 1, 1, 1);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(1, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskNoneCount());
	}

	@Test
	public void testSetReportSecurityMediumRisks() {
		final RiskCounts counts = new RiskCounts(0, 1, 1, 1, 1);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(0, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(1, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskNoneCount());
	}

	@Test
	public void testSetReportMediumRisks() {
		final RiskCounts counts = new RiskCounts(0, 1, 0, 0, 0);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(0, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(1, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(0, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(1, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(0, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(1, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskNoneCount());
	}

	@Test
	public void testSetReportLowRisks() {
		final RiskCounts counts = new RiskCounts(0, 0, 1, 0, 0);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(0, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(1, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(0, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(1, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(0, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(1, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskNoneCount());
	}

	@Test
	public void testSetReportNoneRisks() {
		final RiskCounts counts = new RiskCounts(0, 0, 0, 0, 0);
		final RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
		final RiskProfile riskProfile = new RiskProfile(0, categories);
		final AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null,
				null, null, null, null, riskProfile, null);
		final List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<>();
		aggregateBomViewEntries.add(bomEntry);
		final VersionReport report = new VersionReport(null, aggregateBomViewEntries);
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		hubRiskReportData.setReport(report);

		assertEquals(0, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(0, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(1, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(0, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(0, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(1, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(0, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(0, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(1, hubRiskReportData.getOperationalRiskNoneCount());
	}

	@Test
	public void testReadingFromInputStream() {
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskHighCount", 1000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskMediumCount", 2000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskLowCount", 3000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskNoneCount", 4000);

		TestUtils.setField(hubRiskReportData, "licenseRiskHighCount", 5000);
		TestUtils.setField(hubRiskReportData, "licenseRiskMediumCount", 6000);
		TestUtils.setField(hubRiskReportData, "licenseRiskLowCount", 7000);
		TestUtils.setField(hubRiskReportData, "licenseRiskNoneCount", 8000);

		TestUtils.setField(hubRiskReportData, "operationalRiskHighCount", 9000);
		TestUtils.setField(hubRiskReportData, "operationalRiskMediumCount", 10000);
		TestUtils.setField(hubRiskReportData, "operationalRiskLowCount", 11000);
		TestUtils.setField(hubRiskReportData, "operationalRiskNoneCount", 12000);

		assertEquals(1000, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(2000, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(3000, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(4000, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(5000, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(6000, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(7000, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(8000, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(9000, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(10000, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(11000, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(12000, hubRiskReportData.getOperationalRiskNoneCount());

		final InputStream inputStream = TestUtils
				.getInputStreamFromClasspathFile(XStreamHelperTest.toReadClasspathEntry);
		hubRiskReportData.readFromInputStream(inputStream);
		IOUtils.closeQuietly(inputStream);

		assertEquals(13, hubRiskReportData.getVulnerabilityRiskHighCount());
		assertEquals(14, hubRiskReportData.getVulnerabilityRiskMediumCount());
		assertEquals(15, hubRiskReportData.getVulnerabilityRiskLowCount());
		assertEquals(16, hubRiskReportData.getVulnerabilityRiskNoneCount());

		assertEquals(17, hubRiskReportData.getLicenseRiskHighCount());
		assertEquals(18, hubRiskReportData.getLicenseRiskMediumCount());
		assertEquals(19, hubRiskReportData.getLicenseRiskLowCount());
		assertEquals(20, hubRiskReportData.getLicenseRiskNoneCount());

		assertEquals(21, hubRiskReportData.getOperationalRiskHighCount());
		assertEquals(22, hubRiskReportData.getOperationalRiskMediumCount());
		assertEquals(23, hubRiskReportData.getOperationalRiskLowCount());
		assertEquals(24, hubRiskReportData.getOperationalRiskNoneCount());
	}

	@Test
	public void testWritingToOutputStream() {
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskHighCount", 1000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskMediumCount", 2000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskLowCount", 3000);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskNoneCount", 4000);

		TestUtils.setField(hubRiskReportData, "licenseRiskHighCount", 5000);
		TestUtils.setField(hubRiskReportData, "licenseRiskMediumCount", 6000);
		TestUtils.setField(hubRiskReportData, "licenseRiskLowCount", 7000);
		TestUtils.setField(hubRiskReportData, "licenseRiskNoneCount", 8000);

		TestUtils.setField(hubRiskReportData, "operationalRiskHighCount", 9000);
		TestUtils.setField(hubRiskReportData, "operationalRiskMediumCount", 10000);
		TestUtils.setField(hubRiskReportData, "operationalRiskLowCount", 11000);
		TestUtils.setField(hubRiskReportData, "operationalRiskNoneCount", 12000);

		final OutputStream outputStream = new ByteArrayOutputStream();
		hubRiskReportData.writeToOutputStream(outputStream);
		final String xml = outputStream.toString();
		IOUtils.closeQuietly(outputStream);

		assertTrue(xml.contains("vulnerabilityRiskHighCount"));
		assertTrue(xml.contains("1000"));
		assertTrue(xml.contains("operationalRiskNoneCount"));
		assertTrue(xml.contains("12000"));
	}

}
