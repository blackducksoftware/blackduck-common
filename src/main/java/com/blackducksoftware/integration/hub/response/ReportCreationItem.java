package com.blackducksoftware.integration.hub.response;

public class ReportCreationItem {

    private String reportFormat;

    public ReportCreationItem() {

    }

    public ReportCreationItem(String reportFormat) {
        this.reportFormat = reportFormat;

    }

    public ReportCreationItem(ReportFormatEnum reportFormat) {
        this.reportFormat = reportFormat.name();

    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReportCreationtem [reportFormat=");
        builder.append(reportFormat);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reportFormat == null) ? 0 : reportFormat.hashCode());
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
        ReportCreationItem other = (ReportCreationItem) obj;
        if (reportFormat == null) {
            if (other.reportFormat != null) {
                return false;
            }
        } else if (!reportFormat.equals(other.reportFormat)) {
            return false;
        }
        return true;
    }

}
