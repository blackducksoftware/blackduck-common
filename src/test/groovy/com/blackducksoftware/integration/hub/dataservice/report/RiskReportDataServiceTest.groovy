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
import org.junit.Test

import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.IntLogger

class RiskReportDataServiceTest {
    //    @Rule
    //    public TemporaryFolder folderForReport = new TemporaryFolder()

    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @Test
    public void createReportFilesTest(){
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        RiskReportDataService riskReportDataService = hubServicesFactory.createRiskReportDataService(logger, 30000)
        File folderForReport = new File('.')
        File pdfFile = riskReportDataService.createReportPdfFile(folderForReport, testProjectName, testProjectVersionName)
        Assert.assertNotNull(pdfFile)
        Assert.assertTrue(pdfFile.exists())
    }
}
