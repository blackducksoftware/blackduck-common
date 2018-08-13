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
package com.synopsys.integration.hub.api.codelocation

import com.synopsys.integration.exception.IntegrationException
import com.synopsys.integration.hub.api.generated.component.ProjectRequest
import com.synopsys.integration.hub.api.generated.view.CodeLocationView
import com.synopsys.integration.hub.api.generated.view.ProjectVersionView
import com.synopsys.integration.hub.api.generated.view.ProjectView
import com.synopsys.integration.hub.exception.DoesNotExistException
import com.synopsys.integration.hub.rest.RestConnectionTestHelper
import com.synopsys.integration.hub.service.DryRunUploadResponse
import com.synopsys.integration.hub.service.DryRunUploadService
import com.synopsys.integration.hub.service.HubService
import com.synopsys.integration.hub.service.HubServicesFactory
import com.synopsys.integration.hub.service.ProjectService
import com.synopsys.integration.hub.service.model.ProjectRequestBuilder
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.exception.IntegrationRestException
import com.synopsys.integration.test.annotation.IntegrationTest
import org.apache.commons.lang3.StringUtils
import org.junit.After
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(IntegrationTest.class)
class CodeLocationRequestServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    private static File dryRunFile;

    @BeforeClass
    public static void init() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('dryRun.json').getFile())
    }

    @After
    public void testCleanup() {
        HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger)
        ProjectView project = services.createProjectService().getProjectByName(restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT"))
        services.createProjectService().deleteHubProject(project)
    }

    @Test
    public void testDryRunUpload() {
        final String projectName = restConnectionTestHelper.getProperty("TEST_CREATE_PROJECT");
        final String versionName = restConnectionTestHelper.getProperty("TEST_CREATE_VERSION");

        HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.createHubService(), logger)
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        Assert.assertNotNull(response)

        CodeLocationView codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        ProjectRequestBuilder projectBuilder = new ProjectRequestBuilder()
        projectBuilder.setProjectName(projectName)
        projectBuilder.setVersionName(versionName)

        ProjectVersionView version = getProjectVersion(services.createHubService(), services.createProjectService(), projectBuilder.build())

        services.createCodeLocationService().mapCodeLocation(codeLocationView, version)
        codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isNotBlank(codeLocationView.mappedProjectVersion))

        services.createCodeLocationService().unmapCodeLocation(codeLocationView)
        codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        Assert.assertNotNull(codeLocationView)
        Assert.assertTrue(StringUtils.isBlank(codeLocationView.mappedProjectVersion))

        services.createCodeLocationService().deleteCodeLocation(codeLocationView)
        try {
            services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
            Assert.fail('This should have thrown an exception')
        } catch (IntegrationRestException e) {
            Assert.assertEquals(404, e.getHttpStatusCode())
        }
    }

    private ProjectVersionView getProjectVersion(HubService hubService, ProjectService projectService, final ProjectRequest projectRequest) throws IntegrationException {
        ProjectView project = null
        try {
            project = projectService.getProjectByName(projectRequest.name)
        } catch (final DoesNotExistException e) {
            final String projectURL = projectService.createHubProject(projectRequest)
            project = hubService.getResponse(projectURL, ProjectView.class)
        }
        ProjectVersionView version = null
        try {
            version = projectService.getProjectVersion(project, projectRequest.versionRequest.versionName)
        } catch (final DoesNotExistException e) {
            final String versionURL = projectService.createHubVersion(project, projectRequest.versionRequest)
            version = hubService.getResponse(versionURL, ProjectVersionView.class)
        }
        return version
    }
}
