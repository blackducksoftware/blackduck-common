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
package com.synopsys.integration.blackduck.global;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;

@Tag("integration")
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
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().getUsername());
        assertEquals("Pass", config.getCredentials().getPassword());
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
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().getUsername());
        assertEquals("Pass", config.getCredentials().getPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")), config.getProxyInfo().getPort());
        assertEquals(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"), config.getProxyInfo().getIgnoredProxyHosts());

        assertFalse(config.getProxyInfo().shouldUseProxyForUrl(config.getBlackDuckUrl()));
    }

    @Test
    public void testValidBuildConnect() throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout(120);
        builder.setPassword("blackduck");
        builder.setUsername("sysadmin");
        builder.setTrustCert(true);
        final HubServerConfig config = builder.build();
        assertNotNull(config);
    }

    @Test
    public void testValidBuild() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_INTEGER);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
    }

    @Test
    public void testValidBuildTimeoutString() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(120, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
    }

    @Test
    public void testValidBuildWithProxy() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
        builder.setProxyIgnoredHosts(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"));
        final HubServerConfig config = builder.build();

        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"), String.valueOf(config.getProxyInfo().getPort()));
        assertEquals(restConnectionTestHelper.getProperty("TEST_HTTPS_IGNORE_HOST"), config.getProxyInfo().getIgnoredProxyHosts());
    }

    @Test
    public void testUrlwithTrailingSlash() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final HubServerConfig config = builder.build();
        assertFalse(config.getBlackDuckUrl().toString().endsWith("/"));
        assertEquals("https", config.getBlackDuckUrl().getProtocol());
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(-1, config.getBlackDuckUrl().getPort());
    }

    @Test
    public void testValidBuildWithProxyPortZero() throws Exception {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
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
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("proxy"));
        }
    }

    private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout("100");
        builder.setUsername("User");
        builder.setPassword("Pass");
    }

    private void setBuilderProxyDefaults(final HubServerConfigBuilder builder) throws Exception {
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
    }

}
