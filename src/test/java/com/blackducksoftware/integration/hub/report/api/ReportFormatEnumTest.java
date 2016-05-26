/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *  
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
