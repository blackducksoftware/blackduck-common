/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.dataservice.report

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.IntLogger

class RiskReportDataServiceTestIT {
    @Rule
    public TemporaryFolder folderForReport = new TemporaryFolder()

    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @Test
    public void createReportPdfFileTest(){
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(30000)
        File folderForReport = folderForReport.getRoot()
        File pdfFile = riskReportDataService.createReportPdfFile(folderForReport, testProjectName, testProjectVersionName)
        Assert.assertNotNull(pdfFile)
        Assert.assertTrue(pdfFile.exists())
    }

    @Test
    public void createReportFilesTest(){
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(30000)
        File folderForReport = folderForReport.getRoot()
        riskReportDataService.createReportFiles(folderForReport, testProjectName, testProjectVersionName)

        File[] reportFiles = folderForReport.listFiles();
        Assert.assertNotNull(reportFiles)
        Assert.assertTrue(reportFiles.size() > 0)
        Map<String, File> reportFileMap = reportFiles.collectEntries{
            [it.getName(), it]
        }
        Assert.assertNotNull(reportFileMap.get('js'))
        Assert.assertNotNull(reportFileMap.get('css'))
        Assert.assertNotNull(reportFileMap.get('images'))
        Assert.assertNotNull(reportFileMap.get('riskreport.html'))
    }

    @Test
    public void createNoticesReportFileTest(){
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(30000)
        File folderForReport = folderForReport.getRoot()
        File noticeReportFile = riskReportDataService.createNoticesReportFile(folderForReport, testProjectName, testProjectVersionName);
        Assert.assertNotNull(noticeReportFile)
        Assert.assertTrue(noticeReportFile.exists())
    }
}
