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

import com.blackducksoftware.integration.hub.report.risk.api.RiskCategories;
import com.blackducksoftware.integration.hub.report.risk.api.RiskCounts;
import com.blackducksoftware.integration.hub.report.risk.api.RiskProfile;
import com.blackducksoftware.integration.hub.util.TestUtils;
import com.blackducksoftware.integration.hub.util.XStreamHelperTest;

public class HubBomReportDataTest {
    @Test
    public void testSetReportHighRisks() {
        RiskCounts counts = new RiskCounts(1, 0, 0, 0, 0);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(1, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskNoneCount());

        assertEquals(1, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(0, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(0, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(0, hubBomReportData.getLicenseRiskNoneCount());

        assertEquals(1, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(0, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(0, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(0, hubBomReportData.getOperationalRiskNoneCount());
    }

    @Test
    public void testSetReportSecurityHighRisks() {
        RiskCounts counts = new RiskCounts(1, 1, 1, 1, 1);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(1, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskNoneCount());
    }

    @Test
    public void testSetReportSecurityMediumRisks() {
        RiskCounts counts = new RiskCounts(0, 1, 1, 1, 1);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(0, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(1, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskNoneCount());
    }

    @Test
    public void testSetReportMediumRisks() {
        RiskCounts counts = new RiskCounts(0, 1, 0, 0, 0);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(0, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(1, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskNoneCount());

        assertEquals(0, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(1, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(0, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(0, hubBomReportData.getLicenseRiskNoneCount());

        assertEquals(0, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(1, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(0, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(0, hubBomReportData.getOperationalRiskNoneCount());
    }

    @Test
    public void testSetReportLowRisks() {
        RiskCounts counts = new RiskCounts(0, 0, 1, 0, 0);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(0, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(1, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskNoneCount());

        assertEquals(0, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(0, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(1, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(0, hubBomReportData.getLicenseRiskNoneCount());

        assertEquals(0, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(0, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(1, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(0, hubBomReportData.getOperationalRiskNoneCount());
    }

    @Test
    public void testSetReportNoneRisks() {
        RiskCounts counts = new RiskCounts(0, 0, 0, 0, 0);
        RiskCategories categories = new RiskCategories(counts, counts, counts, counts, counts);
        RiskProfile riskProfile = new RiskProfile(0, categories);
        AggregateBomViewEntry bomEntry = new AggregateBomViewEntry(null, null, null, null, null, null, null, null, null, null, null, null, riskProfile);
        List<AggregateBomViewEntry> aggregateBomViewEntries = new ArrayList<AggregateBomViewEntry>();
        aggregateBomViewEntries.add(bomEntry);
        VersionReport report = new VersionReport(null, aggregateBomViewEntries);
        HubBomReportData hubBomReportData = new HubBomReportData();
        hubBomReportData.setReport(report);

        assertEquals(0, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(0, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(1, hubBomReportData.getVulnerabilityRiskNoneCount());

        assertEquals(0, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(0, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(0, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(1, hubBomReportData.getLicenseRiskNoneCount());

        assertEquals(0, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(0, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(0, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(1, hubBomReportData.getOperationalRiskNoneCount());
    }

    @Test
    public void testReadingFromInputStream() {
        HubBomReportData hubBomReportData = new HubBomReportData();
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskHighCount", 1000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskMediumCount", 2000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskLowCount", 3000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskNoneCount", 4000);

        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskHighCount", 5000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskMediumCount", 6000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskLowCount", 7000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskNoneCount", 8000);

        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskHighCount", 9000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskMediumCount", 10000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskLowCount", 11000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskNoneCount", 12000);

        assertEquals(1000, hubBomReportData.getVulnerabilityRiskHighCount());
        assertEquals(2000, hubBomReportData.getVulnerabilityRiskMediumCount());
        assertEquals(3000, hubBomReportData.getVulnerabilityRiskLowCount());
        assertEquals(4000, hubBomReportData.getVulnerabilityRiskNoneCount());

        assertEquals(5000, hubBomReportData.getLicenseRiskHighCount());
        assertEquals(6000, hubBomReportData.getLicenseRiskMediumCount());
        assertEquals(7000, hubBomReportData.getLicenseRiskLowCount());
        assertEquals(8000, hubBomReportData.getLicenseRiskNoneCount());

        assertEquals(9000, hubBomReportData.getOperationalRiskHighCount());
        assertEquals(10000, hubBomReportData.getOperationalRiskMediumCount());
        assertEquals(11000, hubBomReportData.getOperationalRiskLowCount());
        assertEquals(12000, hubBomReportData.getOperationalRiskNoneCount());

        InputStream inputStream = TestUtils.getInputStreamFromClasspathFile(XStreamHelperTest.toReadClasspathEntry);
        hubBomReportData.readFromInputStream(inputStream);
        IOUtils.closeQuietly(inputStream);

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

    @Test
    public void testWritingToOutputStream() {
        HubBomReportData hubBomReportData = new HubBomReportData();
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskHighCount", 1000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskMediumCount", 2000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskLowCount", 3000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "vulnerabilityRiskNoneCount", 4000);

        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskHighCount", 5000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskMediumCount", 6000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskLowCount", 7000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "licenseRiskNoneCount", 8000);

        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskHighCount", 9000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskMediumCount", 10000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskLowCount", 11000);
        TestUtils.setValue(HubBomReportData.class, hubBomReportData, "operationalRiskNoneCount", 12000);

        OutputStream outputStream = new ByteArrayOutputStream();
        hubBomReportData.writeToOutputStream(outputStream);
        String xml = outputStream.toString();
        IOUtils.closeQuietly(outputStream);

        assertTrue(xml.contains("vulnerabilityRiskHighCount"));
        assertTrue(xml.contains("1000"));
        assertTrue(xml.contains("operationalRiskNoneCount"));
        assertTrue(xml.contains("12000"));
    }

}
