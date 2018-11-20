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
package com.synopsys.integration.blackduck.codelocation

import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper
import com.synopsys.integration.blackduck.service.DryRunUploadResponse
import com.synopsys.integration.blackduck.service.DryRunUploadService
import com.synopsys.integration.blackduck.service.HubServicesFactory
import com.synopsys.integration.log.IntLogger
import com.synopsys.integration.log.LogLevel
import com.synopsys.integration.log.PrintStreamIntLogger
import com.synopsys.integration.rest.exception.IntegrationRestException
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

@Tag("integration")
public class DryRunUploadServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static File dryRunFile;

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    @BeforeAll
    public static void init() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('dryRun.json').getFile())
    }

    @Test
    public void testDryRunUpload() {
        HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger)
        DryRunUploadService dryRunUploadRequestService = new DryRunUploadService(services.createHubService(), logger)
        DryRunUploadResponse response = dryRunUploadRequestService.uploadDryRunFile(dryRunFile)
        assertNotNull(response)

        CodeLocationView codeLocationView = services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
        assertNotNull(codeLocationView)

        //cleanup
        services.createCodeLocationService().deleteCodeLocation(codeLocationView)
        try {
            services.createCodeLocationService().getCodeLocationById(response.codeLocationId)
            fail('This should have thrown an exception')
        } catch (IntegrationRestException e) {
            assertEquals(404, e.getHttpStatusCode())
        }
    }

}
