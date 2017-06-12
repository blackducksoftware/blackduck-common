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
package com.blackducksoftware.integration.hub.scan

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.api.scan.DryRunUploadRequestService
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder
import com.blackducksoftware.integration.hub.global.HubServerConfig
import com.blackducksoftware.integration.hub.model.response.DryRunUploadResponse
import com.blackducksoftware.integration.hub.rest.RestConnection
import com.blackducksoftware.integration.log.IntLogger
import com.blackducksoftware.integration.log.LogLevel
import com.blackducksoftware.integration.log.PrintStreamIntLogger

class DryRunUploadRequestServiceTest {

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO)

    private static File dryRunFile;

    @BeforeClass
    public static void init(){
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader()
        dryRunFile = new File(classLoader.getResource('pcm-jrichard-1-target-2017-06-08T213314.753Z.json').getFile())
    }

    @Test
    public void testDryRunUpload(){
        HubServerConfig config = getHubServerConfig()
        RestConnection restConnection = config.createCredentialsRestConnection(logger)
        DryRunUploadRequestService service = new DryRunUploadRequestService(restConnection)
        DryRunUploadResponse response = service.uploadDryRunFile(dryRunFile)
        Assert.assertNotNull(response)
        println response.toString()
    }

    private HubServerConfig getHubServerConfig(){
        HubServerConfigBuilder builder = new HubServerConfigBuilder()
        builder.setHubUrl('http://int-hub01.dc1.lan:8080')
        builder.setUsername('sysadmin')
        builder.setPassword('blackduck')
        builder.setTimeout(120)
        builder.build()
    }
}
