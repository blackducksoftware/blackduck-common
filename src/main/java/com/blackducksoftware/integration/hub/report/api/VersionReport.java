package com.blackducksoftware.integration.hub.report.api;

import java.util.List;

/**
 * Version report.
 *
 * @author skatzman
 */
public class VersionReport {
    private final DetailedReleaseSummary detailedReleaseSummary;

    private final List<DetailedCodeLocation> detailedCodeLocations;

    private final List<AggregateBomViewEntry> aggregateBomViewEntries;

    private final List<DetailedVulnerability> detailedVulnerabilities;

    private final List<DetailedFileBomViewEntry> detailedFileBomViewEntries;

    public VersionReport(DetailedReleaseSummary detailedReleaseSummary,
            List<DetailedCodeLocation> detailedCodeLocations,
            List<AggregateBomViewEntry> aggregateBomViewEntries,
            List<DetailedVulnerability> detailedVulnerabilities,
            List<DetailedFileBomViewEntry> detailedFileBomViewEntries) {
        this.detailedReleaseSummary = detailedReleaseSummary;
        this.detailedCodeLocations = detailedCodeLocations;
        this.aggregateBomViewEntries = aggregateBomViewEntries;
        this.detailedVulnerabilities = detailedVulnerabilities;
        this.detailedFileBomViewEntries = detailedFileBomViewEntries;
    }

    public DetailedReleaseSummary getDetailedReleaseSummary() {
        return detailedReleaseSummary;
    }

    public List<DetailedCodeLocation> getDetailedCodeLocations() {
        return detailedCodeLocations;
    }

    public List<AggregateBomViewEntry> getAggregateBomViewEntries() {
        return aggregateBomViewEntries;
    }

    public List<DetailedVulnerability> getDetailedVulnerabilities() {
        return detailedVulnerabilities;
    }

    public List<DetailedFileBomViewEntry> getDetailedFileBomViewEntries() {
        return detailedFileBomViewEntries;
    }
}
