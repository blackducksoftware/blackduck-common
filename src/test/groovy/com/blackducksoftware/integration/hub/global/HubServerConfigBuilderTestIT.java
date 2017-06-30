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
package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

import com.blackducksoftware.integration.certificate.CertificateHandler;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class HubServerConfigBuilderTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testValidConfigWithProxies() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        setBuilderDefaults(builder);
        setBuilderProxyDefaults(builder);
        final HubServerConfig config = builder.build();

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals("User", config.getGlobalCredentials().getUsername());
        assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"),
                config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
                config.getProxyInfo().getPort());
    }

    @Test
    public void testValidConfigWithProxiesNoProxy() throws Exception {
        final String ignoreProxyHost = ".*eng-hub-docker-valid02.*";
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        setBuilderDefaults(builder);
        setBuilderProxyDefaults(builder);
        builder.setIgnoredProxyHosts(ignoreProxyHost);
        final HubServerConfig config = builder.build();

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals("User", config.getGlobalCredentials().getUsername());
        assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"),
                config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
                config.getProxyInfo().getPort());
        assertEquals(ignoreProxyHost, config.getProxyInfo().getIgnoredProxyHosts());
        assertFalse(config.getProxyInfo().shouldUseProxyForUrl(config.getHubUrl()));
    }

    @Test
    public void testValidBuildConnect() throws Exception {
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        final URL url = new URL(hubServer);

        final CertificateHandler certHandler = new CertificateHandler(logger);
        if (certHandler.isCertificateInTrustStore(url)) {
            certHandler.removeHttpsCertificate(url);
        }
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl(hubServer);
        builder.setTimeout(120);
        builder.setPassword("blackduck");
        builder.setUsername("sysadmin");
        builder.setAutoImportHttpsCertificates(true);
        final HubServerConfig config = builder.build();
        assertNotNull(config);
    }

    private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setHubUrl(hubServer);
        builder.setTimeout("100");
        builder.setUsername("User");
        builder.setPassword("Pass");
    }

    private void setBuilderProxyDefaults(final HubServerConfigBuilder builder) throws Exception {
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
    }

}
