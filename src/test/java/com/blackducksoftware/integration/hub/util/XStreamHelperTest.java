/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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

import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.util.XStreamHelper;

public class XStreamHelperTest {
    public static final String toWriteClasspathEntry = "com/blackducksoftware/integration/hub/util/XStreamHelperTestToWriteTo.xml";

    public static final String toReadClasspathEntry = "com/blackducksoftware/integration/hub/util/XStreamHelperTestToReadFrom.xml";

    @BeforeClass
    public static void beforeAllTests() throws Exception {
        // test that output file is initially empty
        InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toWriteClasspathEntry);
        String contents = IOUtils.toString(inputStream);
        IOUtils.closeQuietly(inputStream);
        assertTrue(contents.isEmpty());
    }

    @AfterClass
    public static void afterAllTests() throws Exception {
        // clear contents of output file
        OutputStream outputStream = TestUtils.getOutputStreamFromClasspathFile(toWriteClasspathEntry);
        IOUtils.write((String) null, outputStream);
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testWritingXmlToOutputStream() throws IOException {
        HubRiskReportData hubRiskReportData = new HubRiskReportData();
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "vulnerabilityRiskHighCount", 1);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "vulnerabilityRiskMediumCount", 2);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "vulnerabilityRiskLowCount", 3);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "vulnerabilityRiskNoneCount", 4);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "licenseRiskHighCount", 5);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "licenseRiskMediumCount", 6);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "licenseRiskLowCount", 7);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "licenseRiskNoneCount", 8);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "operationalRiskHighCount", 9);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "operationalRiskMediumCount", 10);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "operationalRiskLowCount", 11);
        TestUtils.setValue(HubRiskReportData.class, hubRiskReportData, "operationalRiskNoneCount", 12);

        XStreamHelper<HubRiskReportData> xStreamHelper = new XStreamHelper<HubRiskReportData>();
        String xml = xStreamHelper.toXML(hubRiskReportData);
        assertFalse(xml.isEmpty());

        // write to output file
        OutputStream outputStream = TestUtils.getOutputStreamFromClasspathFile(toWriteClasspathEntry);
        xStreamHelper.toXML(hubRiskReportData, outputStream);
        IOUtils.closeQuietly(outputStream);

        // test that output file is no longer empty
        InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toWriteClasspathEntry);
        String contents = IOUtils.toString(inputStream);
        IOUtils.closeQuietly(inputStream);
        assertFalse(contents.isEmpty());

        assertEquals(xml, contents);
        assertTrue(contents.contains("vulnerabilityRiskHighCount"));
        assertTrue(contents.contains("operationalRiskNoneCount"));
    }

    @Test
    public void testReadingXmlFromInputStream() {
        XStreamHelper<HubRiskReportData> xStreamHelper = new XStreamHelper<HubRiskReportData>();
        InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toReadClasspathEntry);
        HubRiskReportData hubRiskReportData = xStreamHelper.fromXML(inputStream);
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
