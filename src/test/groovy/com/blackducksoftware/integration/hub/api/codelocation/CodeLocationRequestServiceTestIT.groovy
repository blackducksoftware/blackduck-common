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
package com.blackducksoftware.integration.hub.api.codelocation

import org.apache.commons.lang3.StringUtils
import org.junit.After
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.categories.Category

import com.blackducksoftware.integration.IntegrationTest
import com.blackducksoftware.integration.exception.IntegrationException
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadResponse
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadService
import com.blackducksoftware.integration.hub.exception.DoesNotExistException
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException
import com.blackducksoftware.integration.hub.service.HubService
import com.blackducksoftware.integration.hub.service.HubServicesFactory
import com.blackducksoftware.integration.hub.service.ProjectService
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

@Category(IntegrationTest.class)
class CodeLocationRequestServiceTestIT {
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
        HubServicesFactory services = restConnectionTestHelper.createHubDataServicesFactory(logger)
        ProjectView project = services.createProjectDataService().getProjectByName(restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT"))
        services.createProjectDataService().deleteHubProject(project)
    }

    @Test
    public void testDryRunUpload(){
        final String projectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_CREATE_VERSION");

        HubServicesFactory services = restConnectionTestHelper.createHubDataServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.getRestConnection())
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        Assert.assertNotNull(response)

        CodeLocationView codeLocationView = services.createCodeLocationDataService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder()
        projectBuilder.setProjectName(projectName)
        projectBuilder.setVersionName(versionName)

        ProjectVersionView version = getProjectVersion(services.createHubDataService(), services.createProjectDataService(), projectBuilder.build())

        services.createCodeLocationDataService().mapCodeLocation(codeLocationView, version)
        codeLocationView = services.createCodeLocationDataService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isNotBlank(codeLocationView.mappedProjectVersion))

        services.createCodeLocationDataService().unmapCodeLocation(codeLocationView)
        codeLocationView = services.createCodeLocationDataService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        services.createCodeLocationDataService().deleteCodeLocation(codeLocationView)
        try {
            services.createCodeLocationDataService().getCodeLocationById(response.codeLocationId)
            Assert.fail('This should have thrown an exception')
        } catch (IntegrationRestException e){
            Assert.assertEquals(404, e.getHttpStatusCode())
        }
    }

    private ProjectVersionView getProjectVersion(HubService hubService, ProjectService projectDataService, final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null
        try {
            project = projectDataService.getProjectByName(projectRequest.name)
        } catch (final DoesNotExistException e) {
            final String projectURL = projectDataService.createHubProject(projectRequest)
            project = hubService.getResponse(projectURL, ProjectView.class)
        }
        ProjectVersionView version = null
        try {
            version = projectDataService.getProjectVersion(project, projectRequest.versionRequest.versionName)
        } catch (final DoesNotExistException e) {
            final String versionURL = projectDataService.createHubVersion(project, projectRequest.versionRequest)
            version = hubService.getResponse(versionURL, ProjectVersionView.class)
        }
        return version
    }
}
