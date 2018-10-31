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
 * under the License.*/
package com.synopsys.integration.blackduck.dataservice.report

import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.HubServicesFactory
import com.synopsys.integration.blackduck.service.ProjectService
import com.synopsys.integration.blackduck.service.ReportService
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.test.annotation.IntegrationTest
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder

@Category(IntegrationTest.class)
class RiskReportServiceTestIT {
    @Rule
    public TemporaryFolder folderForReport = new TemporaryFolder()

    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper()

    @BeforeClass
    public static void createProjectFirst() {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")
        final String testPhase = restConnectionTestHelper.getProperty("TEST_PHASE")
        final String testDistribution = restConnectionTestHelper.getProperty("TEST_DISTRIBUTION")

        ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(testProjectName);
        projectRequestBuilder.setVersionName(testProjectVersionName);
        projectRequestBuilder.setPhase(testPhase);
        projectRequestBuilder.setDistribution(testDistribution);

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final ProjectService projectService = hubServicesFactory.createProjectService();

        projectService.syncProjectAndVersion(projectRequestBuilder.build(), false);
    }

    @Test
    public void createReportPdfFileTest() {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        ReportService riskReportService = hubServicesFactory.createReportService(30000)
        File folderForReport = folderForReport.getRoot()
        Optional<File> pdfFile = riskReportService.createReportPdfFile(folderForReport, testProjectName, testProjectVersionName)
        Assert.assertTrue(pdfFile.isPresent())
        Assert.assertNotNull(pdfFile.get())
        Assert.assertTrue(pdfFile.get().exists())
    }

    @Test
    public void createReportFilesTest() {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        ReportService riskReportService = hubServicesFactory.createReportService(30000)
        File folderForReport = folderForReport.getRoot()
        riskReportService.createReportFiles(folderForReport, testProjectName, testProjectVersionName)

        File[] reportFiles = folderForReport.listFiles();
        Assert.assertNotNull(reportFiles)
        Assert.assertTrue(reportFiles.size() > 0)
        Map<String, File> reportFileMap = reportFiles.collectEntries {
            [it.getName(), it]
        }
        Assert.assertNotNull(reportFileMap.get('js'))
        Assert.assertNotNull(reportFileMap.get('css'))
        Assert.assertNotNull(reportFileMap.get('images'))
        Assert.assertNotNull(reportFileMap.get('riskreport.html'))
    }

    @Test
    public void createNoticesReportFileTest() {
        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT")
        final String testProjectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION")

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory()
        final IntLogger logger = hubServicesFactory.getRestConnection().logger
        ReportService riskReportService = hubServicesFactory.createReportService(30000)
        File folderForReport = folderForReport.getRoot()
        Optional<File> noticeReportFile = riskReportService.createNoticesReportFile(folderForReport, testProjectName, testProjectVersionName);
        Assert.assertTrue(noticeReportFile.isPresent())
        Assert.assertNotNull(noticeReportFile.get())
        Assert.assertTrue(noticeReportFile.get().exists())
    }

}
