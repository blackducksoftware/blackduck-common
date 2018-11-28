package com.synopsys.integration.blackduck.global;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Tag("integration")
public class BlackDuckServerConfigBuilderTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final String VALID_TIMEOUT_STRING = "120";

    private static final int VALID_TIMEOUT_INTEGER = 120;

    @Test
    public void testValidConfigWithProxies() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setBuilderDefaults(builder);
        setBuilderProxyDefaults(builder);
        final BlackDuckServerConfig config = builder.build();

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().getUsername());
        assertEquals("Pass", config.getCredentials().getPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")), config.getProxyInfo().getPort());

        assertTrue(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidConfigWithProxiesNoProxy() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setBuilderDefaults(builder);
        builder.setProxyPort(ProxyInfo.NO_PROXY_INFO.getPort());
        builder.setProxyHost(ProxyInfo.NO_PROXY_INFO.getHost());
        builder.setProxyNtlmDomain(ProxyInfo.NO_PROXY_INFO.getNtlmDomain());
        builder.setProxyNtlmWorkstation(ProxyInfo.NO_PROXY_INFO.getNtlmDomain());
        builder.setProxyUsername(ProxyInfo.NO_PROXY_INFO.getUsername());
        builder.setProxyPassword(ProxyInfo.NO_PROXY_INFO.getPassword());
        final BlackDuckServerConfig config = builder.build();

        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().getUsername());
        assertEquals("Pass", config.getCredentials().getPassword());

        assertFalse(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidBuildConnect() throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout(120);
        builder.setPassword("blackduck");
        builder.setUsername("sysadmin");
        builder.setTrustCert(true);
        final BlackDuckServerConfig config = builder.build();
        assertNotNull(config);
    }

    @Test
    public void testValidBuild() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_INTEGER);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final BlackDuckServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
    }

    @Test
    public void testValidBuildTimeoutString() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final BlackDuckServerConfig config = builder.build();
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(120, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
    }

    @Test
    public void testValidBuildWithProxy() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
        final BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(VALID_TIMEOUT_INTEGER, config.getTimeout());
        assertEquals(restConnectionTestHelper.getProperty("TEST_USERNAME"), config.getCredentials().getUsername());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PASSWORD"), config.getCredentials().getPassword());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"), String.valueOf(config.getProxyInfo().getPort()));
    }

    @Test
    public void testUrlwithTrailingSlash() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setTimeout(VALID_TIMEOUT_STRING);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        final BlackDuckServerConfig config = builder.build();
        assertFalse(config.getBlackDuckUrl().toString().endsWith("/"));
        assertEquals("https", config.getBlackDuckUrl().getProtocol());
        assertEquals(new URL(hubServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(-1, config.getBlackDuckUrl().getPort());
    }

    @Test
    public void testValidBuildWithProxyPortZero() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setTrustCert(true);
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setUrl(hubServer);
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        BlackDuckServerConfig config = builder.build();
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

    private void setBuilderDefaults(final BlackDuckServerConfigBuilder builder) throws Exception {
        final String hubServer = restConnectionTestHelper.getProperty("TEST_HTTPS_HUB_SERVER_URL");
        builder.setTrustCert(true);
        builder.setUrl(hubServer);
        builder.setTimeout("100");
        builder.setUsername("User");
        builder.setPassword("Pass");
    }

    private void setBuilderProxyDefaults(final BlackDuckServerConfigBuilder builder) throws Exception {
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(NumberUtils.toInt(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
    }

}
