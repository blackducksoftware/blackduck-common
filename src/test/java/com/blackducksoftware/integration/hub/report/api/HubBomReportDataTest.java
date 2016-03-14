package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.report.risk.api.RiskCategories;
import com.blackducksoftware.integration.hub.report.risk.api.RiskCounts;
import com.blackducksoftware.integration.hub.report.risk.api.RiskProfile;

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

}
