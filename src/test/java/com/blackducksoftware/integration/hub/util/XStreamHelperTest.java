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
package com.blackducksoftware.integration.hub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.test.TestUtils;

public class XStreamHelperTest {
	public static final String toWriteClasspathEntry = "com/blackducksoftware/integration/hub/util/XStreamHelperTestToWriteTo.xml";

	public static final String toReadClasspathEntry = "com/blackducksoftware/integration/hub/util/XStreamHelperTestToReadFrom.xml";

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		// test that output file is initially empty
		final InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toWriteClasspathEntry);
		final String contents = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		assertTrue(contents.isEmpty());
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		// clear contents of output file
		final OutputStream outputStream = TestUtils.getOutputStreamFromClasspathFile(toWriteClasspathEntry);
		IOUtils.write((String) null, outputStream);
		IOUtils.closeQuietly(outputStream);
	}

	@Test
	public void testWritingXmlToOutputStream() throws IOException {
		final HubRiskReportData hubRiskReportData = new HubRiskReportData();
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskHighCount", 1);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskMediumCount", 2);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskLowCount", 3);
		TestUtils.setField(hubRiskReportData, "vulnerabilityRiskNoneCount", 4);
		TestUtils.setField(hubRiskReportData, "licenseRiskHighCount", 5);
		TestUtils.setField(hubRiskReportData, "licenseRiskMediumCount", 6);
		TestUtils.setField(hubRiskReportData, "licenseRiskLowCount", 7);
		TestUtils.setField(hubRiskReportData, "licenseRiskNoneCount", 8);
		TestUtils.setField(hubRiskReportData, "operationalRiskHighCount", 9);
		TestUtils.setField(hubRiskReportData, "operationalRiskMediumCount", 10);
		TestUtils.setField(hubRiskReportData, "operationalRiskLowCount", 11);
		TestUtils.setField(hubRiskReportData, "operationalRiskNoneCount", 12);

		final XStreamHelper<HubRiskReportData> xStreamHelper = new XStreamHelper<>();
		final String xml = xStreamHelper.toXML(hubRiskReportData);
		assertFalse(xml.isEmpty());

		// write to output file
		final OutputStream outputStream = TestUtils.getOutputStreamFromClasspathFile(toWriteClasspathEntry);
		xStreamHelper.toXML(hubRiskReportData, outputStream);
		IOUtils.closeQuietly(outputStream);

		// test that output file is no longer empty
		final InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toWriteClasspathEntry);
		final String contents = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		assertFalse(contents.isEmpty());

		assertEquals(xml, contents);
		assertTrue(contents.contains("vulnerabilityRiskHighCount"));
		assertTrue(contents.contains("operationalRiskNoneCount"));
	}

	@Test
	public void testReadingXmlFromInputStream() {
		final XStreamHelper<HubRiskReportData> xStreamHelper = new XStreamHelper<>();
		final InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toReadClasspathEntry);
		final HubRiskReportData hubRiskReportData = xStreamHelper.fromXML(inputStream);
		IOUtils.closeQuietly(inputStream);

		assertNull(hubRiskReportData.getReport());
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

}
