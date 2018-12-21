package com.synopsys.integration.blackduck.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.rest.TestingPropertyKey;
import com.synopsys.integration.log.SilentIntLogger;

@Tag("integration")
public class BlackDuckServerConfigBuilderTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private static final String URL = restConnectionTestHelper.getIntegrationBlackDuckServerUrl();
    private static final String USERNAME = restConnectionTestHelper.getTestUsername();
    private static final String PASSWORD = restConnectionTestHelper.getTestPassword();
    private static final String PROXY_PASSTHROUGH_HOST = restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_PROXY_HOST_PASSTHROUGH);
    private static final int PROXY_PASSTHROUGH_PORT = NumberUtils.toInt(restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_PROXY_PORT_PASSTHROUGH));
    private static final int TIMEOUT = 120;

    @Test
    public void testValidConfigWithProxies() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setBuilderDefaults(builder);
        setBuilderProxyDefaults(builder);
        final BlackDuckServerConfig config = builder.build();

        final String blackDuckServer = URL;
        assertEquals(new URL(blackDuckServer).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().get().getUsername().get());
        assertEquals("Pass", config.getCredentials().get().getPassword().get());
        assertEquals(PROXY_PASSTHROUGH_HOST, config.getProxyInfo().getHost().get());
        assertEquals(PROXY_PASSTHROUGH_PORT, config.getProxyInfo().getPort());

        assertTrue(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidConfigWithProxiesNoProxy() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setBuilderDefaults(builder);
        builder.setProxyPort(0);
        builder.setProxyHost(null);
        builder.setProxyNtlmDomain(null);
        builder.setProxyNtlmWorkstation(null);
        builder.setProxyUsername(null);
        builder.setProxyPassword(null);
        final BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(URL).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals("User", config.getCredentials().get().getUsername().get());
        assertEquals("Pass", config.getCredentials().get().getPassword().get());

        assertFalse(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidCanConnect() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        final BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertTrue(blackDuckServerConfig.canConnect());
        final ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertTrue(connectionResult.isSuccess());
        assertFalse(connectionResult.getErrorMessage().isPresent());
    }

    @Test
    public void testInvalidUrlCanNotConnect() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        builder.setUrl("https://www.google.com");
        final BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        final ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals("The connection was not successful for an unknown reason.", connectionResult.getErrorMessage().get());
    }

    @Test
    public void testInvalidPasswordCanNotConnect() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        builder.setPassword("not a real password");
        final BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        final ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals("Invalid username or password", connectionResult.getErrorMessage().get());
    }

    @Test
    public void testInvalidApiTokenCanNotConnect() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        builder.setUsername(null);
        builder.setPassword(null);
        builder.setApiToken("not a real token");
        final BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        final ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals("The connection was not successful for an unknown reason.", connectionResult.getErrorMessage().get());
    }

    @Test
    public void testValidBuild() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        final BlackDuckServerConfig config = builder.build();
        assertEquals(new URL(URL).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(TIMEOUT, config.getTimeout());
        assertEquals(USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(PASSWORD, config.getCredentials().get().getPassword().get());
    }

    @Test
    public void testValidBuildTimeoutString() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        final BlackDuckServerConfig config = builder.build();
        assertEquals(new URL(URL).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(120, config.getTimeout());
        assertEquals(USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(PASSWORD, config.getCredentials().get().getPassword().get());
    }

    @Test
    public void testValidBuildWithProxy() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        builder.setProxyHost(PROXY_PASSTHROUGH_HOST);
        builder.setProxyPort(PROXY_PASSTHROUGH_PORT);
        final BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(URL).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(TIMEOUT, config.getTimeout());
        assertEquals(USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(PASSWORD, config.getCredentials().get().getPassword().get());
        assertEquals(PROXY_PASSTHROUGH_HOST, config.getProxyInfo().getHost().get());
        assertEquals(PROXY_PASSTHROUGH_PORT, config.getProxyInfo().getPort());
    }

    @Test
    public void testUrlwithTrailingSlash() throws Exception {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        final BlackDuckServerConfig config = builder.build();
        assertFalse(config.getBlackDuckUrl().toString().endsWith("/"));
        assertEquals("https", config.getBlackDuckUrl().getProtocol());
        assertEquals(new URL(URL).getHost(), config.getBlackDuckUrl().getHost());
        assertEquals(-1, config.getBlackDuckUrl().getPort());
    }

    @Test
    public void testValidBuildWithProxyPortZero() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        BlackDuckServerConfig config = builder.build();
        assertFalse(config.shouldUseProxyForBlackDuck());

        builder.setProxyPort(0);
        config = builder.build();
        assertFalse(config.shouldUseProxyForBlackDuck());

        builder.setProxyPort("0");
        config = builder.build();
        assertFalse(config.shouldUseProxyForBlackDuck());

        builder.setProxyPort(1);
        try {
            builder.build();
            fail("Should have thrown an IllegalStateException with invalid proxy state");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("proxy"));
        }
    }

    private void setValidDefaults(final BlackDuckServerConfigBuilder builder) {
        builder.setUrl(URL);
        builder.setUsername(USERNAME);
        builder.setPassword(PASSWORD);
        builder.setTrustCert(true);
    }

    private void setBuilderDefaults(final BlackDuckServerConfigBuilder builder) {
        setValidDefaults(builder);
        builder.setTimeout("100");
        builder.setUsername("User");
        builder.setPassword("Pass");
    }

    private void setBuilderProxyDefaults(final BlackDuckServerConfigBuilder builder) {
        builder.setProxyHost(PROXY_PASSTHROUGH_HOST);
        builder.setProxyPort(PROXY_PASSTHROUGH_PORT);
    }

}
