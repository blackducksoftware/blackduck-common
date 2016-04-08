/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.report.api.ReportFormatEnum;

public class ReportFormatEnumTest {

    @Test
    public void testGetReportFormatEnum() {
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum("Fake"));
        assertEquals(ReportFormatEnum.CSV, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.CSV.toString().toLowerCase()));
        assertEquals(ReportFormatEnum.CSV, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.CSV.toString()));
        assertEquals(ReportFormatEnum.JSON, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.JSON.toString().toLowerCase()));
        assertEquals(ReportFormatEnum.JSON, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.JSON.toString()));
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.UNKNOWN.toString()));
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum(ReportFormatEnum.UNKNOWN.toString().toLowerCase()));
        assertEquals(ReportFormatEnum.UNKNOWN, ReportFormatEnum.getReportFormatEnum(null));
    }
}
