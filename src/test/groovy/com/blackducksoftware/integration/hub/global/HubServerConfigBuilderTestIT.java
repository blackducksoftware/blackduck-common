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

import static org.junit.Assert.*;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class HubServerConfigBuilderTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final String VALID_TIMEOUT_STRING = "120";

    private static final int VALID_TIMEOUT_INTEGER = 120;

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
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")), config.getProxyInfo().getPort());
    }

    @Test
    public void testValidConfigWithProxiesNoProxy() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        setBuilderDefaults(builder);
        setBuilderProxyDefaults(builder);
        builder.setProxyIgnoredHosts(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"));
        final HubServerConfig config = builder.build();

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals("User", config.getGlobalCredentials().getUsername());
        assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")), config.getProxyInfo().getPort());
        assertEquals(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"), config.getProxyInfo().getIgnoredProxyHosts());

        assertFalse(config.getProxyInfo().shouldUseProxyForUrl(config.getHubUrl()));
    }

    @Test
    public void testValidBuildConnect() throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        builder.setHubUrl(hubServer);
        builder.setTimeout(120);
        builder.setPassword("blackduck");
        builder.setUsername("sysadmin");
        builder.setAlwaysTrustServerCertificate(true);
        final HubServerConfig config = builder.build();
        assertNotNull(config);
    }

    @Test
    public void testValidBuild() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setAlwaysTrustServerCertificate(true);
        builder.setHubUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_INTEGER);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getGlobalCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getGlobalCredentials().getDecryptedPassword());
    }

    @Test
    public void testValidBuildTimeoutString() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setHubUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals(120, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getGlobalCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getGlobalCredentials().getDecryptedPassword());
    }

    @Test
    public void testValidBuildWithProxy() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setHubUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
        builder.setProxyIgnoredHosts(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"));
        final HubServerConfig config = builder.build();

        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getGlobalCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getGlobalCredentials().getDecryptedPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"), String.valueOf(config.getProxyInfo().getPort()));
        assertEquals(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"), config.getProxyInfo().getIgnoredProxyHosts());
    }

    @Test
    public void testUrlwithTrailingSlash() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setHubUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertFalse(config.getHubUrl().toString().endsWith("/"));
        assertEquals("https", config.getHubUrl().getProtocol());
        assertEquals(new URL(hubServer).getHost(), config.getHubUrl().getHost());
        assertEquals(-1, config.getHubUrl().getPort());
    }

    @Test
    public void testValidBuildWithProxyPortZero() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setAlwaysTrustServerCertificate(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setHubUrl(hubServer);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        HubServerConfig config = builder.build();
        assertFalse(config.shouldUseProxyForHub());

        builder.setProxyPort(0);
        config = builder.build();
        assertFalse(config.shouldUseProxyForHub());

        builder.setProxyPort("0");
        config = builder.build();
        assertFalse(config.shouldUseProxyForHub());

        builder.setProxyPort(1);
        try {
            config = builder.build();
            fail("Should have thrown an IllegalStateException with invalid proxy state");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("proxy"));
        }
    }

    private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setAlwaysTrustServerCertificate(true);
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
