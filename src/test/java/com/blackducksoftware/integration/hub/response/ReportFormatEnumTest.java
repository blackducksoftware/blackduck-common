package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReportFormatEnumTest {

    @Test
    public void testGetReportFormatEnum() {
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum("Fake"));
        assertEquals(ReportFormatEnum.CSV, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.CSV.toString().toLowerCase()));
        assertEquals(ReportFormatEnum.CSV, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.CSV.toString()));
        assertEquals(ReportFormatEnum.JSON, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.JSON.toString()));
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.UNKNOWN.toString()));
        assertEquals(ReportFormatEnum.JSON, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.JSON.toString().toLowerCase()));
    }
}
