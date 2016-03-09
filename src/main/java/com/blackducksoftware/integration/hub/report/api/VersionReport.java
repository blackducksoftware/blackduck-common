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

    private final List<AggregateBomViewEntry> aggregateBomViewEntries;

    public VersionReport(DetailedReleaseSummary detailedReleaseSummary,
            // List<DetailedCodeLocation> detailedCodeLocations,
            List<AggregateBomViewEntry> aggregateBomViewEntries) {
        this.detailedReleaseSummary = detailedReleaseSummary;
        this.aggregateBomViewEntries = aggregateBomViewEntries;
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

    public String getReportProjectUrl() {
        if (detailedReleaseSummary == null || StringUtils.isBlank(getBaseUrl())
                || StringUtils.isBlank(detailedReleaseSummary.getProjectId())) {
            return null;
        }

        URLBuilder builder = new URLBuilder(getBaseUrl());
        builder.setFragment("projects/id:" + detailedReleaseSummary.getProjectId());

        return builder.buildURL();
    }

    public String getReportVersionUrl() {
        if (detailedReleaseSummary == null || StringUtils.isBlank(getBaseUrl())
                || StringUtils.isBlank(detailedReleaseSummary.getVersionId())) {
            return null;
        }

        URLBuilder builder = new URLBuilder(getBaseUrl());
        builder.setFragment("versions/id:" + detailedReleaseSummary.getVersionId() + "/view:bom");

        return builder.buildURL();
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
                entry.getProducerReleases() == null || StringUtils.isBlank(entry.getProducerReleasesId())) {
            return null;
        }

        URLBuilder builder = new URLBuilder(getBaseUrl());
        builder.setFragment("versions/id:" + entry.getProducerReleasesId());

        return builder.buildURL();
    }

    public List<AggregateBomViewEntry> getAggregateBomViewEntries() {
        return aggregateBomViewEntries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aggregateBomViewEntries == null) ? 0 : aggregateBomViewEntries.hashCode());
        result = prime * result + ((detailedReleaseSummary == null) ? 0 : detailedReleaseSummary.hashCode());
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
        if (!(obj instanceof VersionReport)) {
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
        if (detailedReleaseSummary == null) {
            if (other.detailedReleaseSummary != null) {
                return false;
            }
        } else if (!detailedReleaseSummary.equals(other.detailedReleaseSummary)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionReport [detailedReleaseSummary=");
        builder.append(detailedReleaseSummary);
        builder.append(", aggregateBomViewEntries=");
        builder.append(aggregateBomViewEntries);
        builder.append("]");
        return builder.toString();
    }

}
