package com.blackducksoftware.integration.hub.response;

public enum ReportFormatEnum {

    CSV,
    JSON,
    UNKNOWN;

    public static ReportFormatEnum getReportFormatEnum(String reportFormatEnum) {
        ReportFormatEnum reportFormat = UNKNOWN;
        try {
            reportFormat = ReportFormatEnum.valueOf(reportFormatEnum.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ignore expection
        }
        return reportFormat;
    }

}
