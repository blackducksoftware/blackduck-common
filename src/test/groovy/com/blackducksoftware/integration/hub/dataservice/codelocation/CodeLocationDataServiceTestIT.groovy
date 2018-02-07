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
package com.blackducksoftware.integration.hub.dataservice.codelocation

import org.apache.commons.lang3.StringUtils
import org.junit.After
import org.junit.Assert;
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.IntegrationTest
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadResponse
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadService
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

@Category(IntegrationTest.class)
class CodeLocationDataServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    private static File dryRunFile;

    @BeforeClass
    public static void init(){
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('dryRun.json').getFile())
    }

    @After
    public void testCleanup(){
        HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger)
        ProjectDataService projectDataService = services.createProjectDataService()
        ProjectView project = projectDataService.getProjectByName(restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT"))
        projectDataService.deleteHubProject(project)
    }

    @Test
    public void testDryRunUpload(){
        final String projectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_CREATE_VERSION");

        HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.getRestConnection())
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        Assert.assertNotNull(response)

        CodeLocationDataService codeLocationDataService = services.createCodeLocationDataService()
        CodeLocationView codeLocationView = codeLocationDataService.getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder()
        projectBuilder.setProjectName(projectName)
        projectBuilder.setVersionName(versionName)

        ProjectDataService projectDataService = services.createProjectDataService()
        ProjectVersionWrapper projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectBuilder.build());
        ProjectVersionView version = projectVersionWrapper.getProjectVersionView()

        codeLocationDataService.mapCodeLocation(codeLocationView, version)
        codeLocationView = codeLocationDataService.getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isNotBlank(codeLocationView.mappedProjectVersion))

        codeLocationDataService.unmapCodeLocation(codeLocationView)
        codeLocationView = codeLocationDataService.getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        codeLocationDataService.deleteCodeLocation(codeLocationView)
        try {
            codeLocationDataService.getCodeLocationById(response.codeLocationId)
            Assert.fail('This should have thrown an exception')
        } catch (IntegrationRestException e){
            Assert.assertEquals(404, e.getHttpStatusCode())
        }
    }
}
