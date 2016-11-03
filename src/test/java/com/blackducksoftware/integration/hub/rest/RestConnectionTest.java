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
package com.blackducksoftware.integration.hub.rest;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.util.HubUrlParser;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.test.TestLogger;

public class RestConnectionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testGetBaseUrlWithPort() throws URISyntaxException {
        final String urlPrefix = "https://hub.bds.com:8080/";
        final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
        final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

        assertEquals(urlPrefix, HubUrlParser.getBaseUrl(projectVersionUrl));
    }

    @Test
    public void testGetBaseUrlWithoutPort() throws URISyntaxException {
        final String urlPrefix = "https://hub.bds.com/";
        final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
        final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

        assertEquals(urlPrefix, HubUrlParser.getBaseUrl(projectVersionUrl));
    }

    @Test
    public void testGetRelativeUrl() throws URISyntaxException {
        final String urlPrefix = "https://hub.bds.com:8080/";
        final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
        final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

        assertEquals(projectVersionRelativeUrl, HubUrlParser.getRelativeUrl(projectVersionUrl));
    }

    @Test
    public void testGetRelativeUrlTrailingSlashNormalization() throws URISyntaxException {
        final String urlPrefix = "https://hub.bds.com:8080/";
        final String projectVersionRelativeUrl = "api/projects/1234/versions/5678" + "/";
        final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

        assertEquals(projectVersionRelativeUrl, HubUrlParser.getRelativeUrl(projectVersionUrl));
    }
    
    // TODO this is an integration test
    @Test
    public void testConnectionParameters() throws IllegalArgumentException, URISyntaxException, BDRestException, EncryptionException {
        IntLogger logger = new TestLogger();
        logger.setLogLevel(LogLevel.TRACE);
        HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl("https://integration-hub.blackducksoftware.com:8443");
        builder.setUsername("sysadmin");
        builder.setPassword("blackduck");
        builder.setTimeout(33);
        HubServerConfig config = builder.build();
        RestConnection conn = new CredentialsRestConnection(logger , config);
    }
}
