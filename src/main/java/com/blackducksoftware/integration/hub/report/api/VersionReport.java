package com.blackducksoftware.integration.hub.report.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.util.URLBuilder;

/**
 * Version report.
 *
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

    public String getBaseUrl() {
        if (detailedReleaseSummary == null || detailedReleaseSummary.getUiUrlGenerator() == null) {
            return null;
        }
        return detailedReleaseSummary.getUiUrlGenerator().getBaseUrl();
    }

    public String getComponentUrl(AggregateBomViewEntry entry) {
        if (StringUtils.isBlank(getBaseUrl()) || entry == null ||
                entry.getProducerProject() == null || StringUtils.isBlank(entry.getProducerProject().getId())) {
            return null;
        }

        URLBuilder builder = new URLBuilder(getBaseUrl());
        builder.setFragment("projects/id:" + entry.getProducerProject().getId());

        return builder.buildURL();
    }

    public String getVersionUrl(AggregateBomViewEntry entry) {
        if (StringUtils.isBlank(getBaseUrl()) || entry == null ||
                entry.getProducerProject() == null || StringUtils.isBlank(entry.getProducerProject().getId())) {
            return null;
        }

        URLBuilder builder = new URLBuilder(getBaseUrl());
        builder.setFragment("versions/id:" + entry.getProducerReleasesId());

        return builder.buildURL();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateBomViewEntries == null) ? 0 : aggregateBomViewEntries.hashCode());
        result = prime * result + ((detailedCodeLocations == null) ? 0 : detailedCodeLocations.hashCode());
        result = prime * result + ((detailedFileBomViewEntries == null) ? 0 : detailedFileBomViewEntries.hashCode());
        result = prime * result + ((detailedReleaseSummary == null) ? 0 : detailedReleaseSummary.hashCode());
        result = prime * result + ((detailedVulnerabilities == null) ? 0 : detailedVulnerabilities.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VersionReport other = (VersionReport) obj;
        if (aggregateBomViewEntries == null) {
            if (other.aggregateBomViewEntries != null) {
                return false;
            }
        } else if (!aggregateBomViewEntries.equals(other.aggregateBomViewEntries)) {
            return false;
        }
        if (detailedCodeLocations == null) {
            if (other.detailedCodeLocations != null) {
                return false;
            }
        } else if (!detailedCodeLocations.equals(other.detailedCodeLocations)) {
            return false;
        }
        if (detailedFileBomViewEntries == null) {
            if (other.detailedFileBomViewEntries != null) {
                return false;
            }
        } else if (!detailedFileBomViewEntries.equals(other.detailedFileBomViewEntries)) {
            return false;
        }
        if (detailedReleaseSummary == null) {
            if (other.detailedReleaseSummary != null) {
                return false;
            }
        } else if (!detailedReleaseSummary.equals(other.detailedReleaseSummary)) {
            return false;
        }
        if (detailedVulnerabilities == null) {
            if (other.detailedVulnerabilities != null) {
                return false;
            }
        } else if (!detailedVulnerabilities.equals(other.detailedVulnerabilities)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionReport [detailedReleaseSummary=");
        builder.append(detailedReleaseSummary);
        builder.append(", detailedCodeLocations=");
        builder.append(detailedCodeLocations);
        builder.append(", aggregateBomViewEntries=");
        builder.append(aggregateBomViewEntries);
        builder.append(", detailedVulnerabilities=");
        builder.append(detailedVulnerabilities);
        builder.append(", detailedFileBomViewEntries=");
        builder.append(detailedFileBomViewEntries);
        builder.append("]");
        return builder.toString();
    }

}
