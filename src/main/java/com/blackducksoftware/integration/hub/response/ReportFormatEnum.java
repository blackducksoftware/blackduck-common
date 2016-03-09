package com.blackducksoftware.integration.hub.response;

public enum ReportFormatEnum {

    CSV,
    JSON,
    UNKNOWN;

    public static ReportFormatEnum getReportFormatEnum(String reportFormatEnum) {
        if (reportFormatEnum == null) {
            return ReportFormatEnum.UNKNOWN;
        }
        ReportFormatEnum reportFormat;
        try {
            reportFormat = ReportFormatEnum.valueOf(reportFormatEnum.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ignore expection
            reportFormat = UNKNOWN;
        }
        return reportFormat;
    }

}
