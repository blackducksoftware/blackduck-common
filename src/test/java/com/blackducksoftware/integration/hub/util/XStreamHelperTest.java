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

import com.blackducksoftware.integration.hub.report.api.HubBomReportData;
import com.blackducksoftware.integration.util.XStreamHelper;

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
        HubBomReportData hubBomReportData = new HubBomReportData();
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskHighCount", 1);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskMediumCount", 2);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskLowCount", 3);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskNoneCount", 4);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskHighCount", 5);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskMediumCount", 6);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskLowCount", 7);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskNoneCount", 8);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskHighCount", 9);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskMediumCount", 10);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskLowCount", 11);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskNoneCount", 12);

        XStreamHelper<HubBomReportData> xStreamHelper = new XStreamHelper<HubBomReportData>();
        String xml = xStreamHelper.toXML(hubBomReportData);
        assertFalse(xml.isEmpty());

        // write to output file
        OutputStream outputStream = TestUtils.getOutputStreamFromClasspathFile(toWriteClasspathEntry);
        xStreamHelper.toXML(hubBomReportData, outputStream);
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
        XStreamHelper<HubBomReportData> xStreamHelper = new XStreamHelper<HubBomReportData>();
        InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(toReadClasspathEntry);
        HubBomReportData hubBomReportData = xStreamHelper.fromXML(inputStream);
        IOUtils.closeQuietly(inputStream);

        assertNull(hubBomReportData.getReport());
        assertEquals(13, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(14, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(15, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(16, hubBomReportData.getVulnerabilityRiskNoneCount());
        assertEquals(17, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(18, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(19, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(20, hubBomReportData.getLicenseRiskNoneCount());
        assertEquals(21, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(22, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(23, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(24, hubBomReportData.getOperationalRiskNoneCount());
    }

}
