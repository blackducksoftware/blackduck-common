package com.blackducksoftware.integration.hub.response;

public enum ReportFormatEnum {

    CSV,
    JSON,
    UNKNOWN;

    public static ReportFormatEnum getReportFormatEnum(String reportFormatEnum) {
        if (reportFormatEnum.equalsIgnoreCase(CSV.name())) {
            return ReportFormatEnum.CSV;
        } else if (reportFormatEnum.equalsIgnoreCase(JSON.name())) {
            return ReportFormatEnum.JSON;
        } else {
            return ReportFormatEnum.UNKNOWN;
        }
    }

}
