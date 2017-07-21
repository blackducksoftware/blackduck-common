/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(logger, 30000)
        //File folderForReport = folderForReport.getRoot()
        File folderForReport = new File('.')
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
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(logger, 30000)
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
}
