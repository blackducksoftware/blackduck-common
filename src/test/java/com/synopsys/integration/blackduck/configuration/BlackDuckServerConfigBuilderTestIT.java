package com.synopsys.integration.blackduck.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.http.client.TestingPropertyKey;
import com.synopsys.integration.blackduck.useragent.BlackDuckCommon;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.ConnectionResult;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
@ExtendWith(TimingExtension.class)
public class BlackDuckServerConfigBuilderTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();
    private final HttpUrl URL = BlackDuckServerConfigBuilderTestIT.INT_HTTP_CLIENT_TEST_HELPER.getIntegrationBlackDuckServerUrl();
    private static final String USERNAME = BlackDuckServerConfigBuilderTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestUsername();
    private static final String PASSWORD = BlackDuckServerConfigBuilderTestIT.INT_HTTP_CLIENT_TEST_HELPER.getTestPassword();
    private static final String PROXY_PASSTHROUGH_HOST = BlackDuckServerConfigBuilderTestIT.INT_HTTP_CLIENT_TEST_HELPER.getProperty(TestingPropertyKey.TEST_PROXY_HOST_PASSTHROUGH);
    private static final int PROXY_PASSTHROUGH_PORT = NumberUtils.toInt(BlackDuckServerConfigBuilderTestIT.INT_HTTP_CLIENT_TEST_HELPER.getProperty(TestingPropertyKey.TEST_PROXY_PORT_PASSTHROUGH));
    private static final int TIMEOUT = 120;

    public BlackDuckServerConfigBuilderTestIT() throws IntegrationException {

    }

    @Test
    public void testValidBuild() throws Exception {
        BlackDuckServerConfigBuilder builder = createValid();
        BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(URL.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals(BlackDuckServerConfigBuilderTestIT.TIMEOUT, config.getTimeout());
        assertEquals(BlackDuckServerConfigBuilderTestIT.USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PASSWORD, config.getCredentials().get().getPassword().get());

        assertTrue(config.canConnect());
    }

    @Test
    public void testValidConfigWithProxies() throws Exception {
        BlackDuckServerConfigBuilder builder = createBuilder();
        setBuilderProxyDefaults(builder);
        BlackDuckServerConfig config = builder.build();

        HttpUrl blackDuckServer = URL;
        assertEquals(new URL(blackDuckServer.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals("User", config.getCredentials().get().getUsername().get());
        assertEquals("Pass", config.getCredentials().get().getPassword().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_HOST, config.getProxyInfo().getHost().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_PORT, config.getProxyInfo().getPort());

        assertTrue(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidConfigWithProxiesNoProxy() throws Exception {
        BlackDuckServerConfigBuilder builder = createBuilder();
        builder.setProxyPort(0);
        builder.setProxyHost(null);
        builder.setProxyNtlmDomain(null);
        builder.setProxyNtlmWorkstation(null);
        builder.setProxyUsername(null);
        builder.setProxyPassword(null);
        BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(URL.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals("User", config.getCredentials().get().getUsername().get());
        assertEquals("Pass", config.getCredentials().get().getPassword().get());

        assertFalse(config.getProxyInfo().shouldUseProxy());
    }

    @Test
    public void testValidCanConnect() {
        BlackDuckServerConfigBuilder builder = createValid();
        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertTrue(blackDuckServerConfig.canConnect());
        ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertTrue(connectionResult.isSuccess());
        assertFalse(connectionResult.getFailureMessage().isPresent());
    }

    @Test
    public void testInvalidUrlCanNotConnect() {
        BlackDuckServerConfigBuilder builder = createValid();
        builder.setUrl("https://www.google.com");
        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals("The connection was not successful for an unknown reason. If an api token is being used, it could be incorrect.", connectionResult.getFailureMessage().get());
    }

    @Test
    public void testInvalidPasswordCanNotConnect() {
        BlackDuckServerConfigBuilder builder = createValid();
        builder.setPassword("not a real password");
        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals("Invalid username or password", connectionResult.getFailureMessage().get());
    }

    @Test
    public void testInvalidApiTokenCanNotConnect() {
        BlackDuckServerConfigBuilder builder = createValid();
        builder.setUsername(null);
        builder.setPassword(null);
        builder.setApiToken("not a real token");
        BlackDuckServerConfig blackDuckServerConfig = builder.build();
        assertFalse(blackDuckServerConfig.canConnect());
        ConnectionResult connectionResult = blackDuckServerConfig.attemptConnection(new SilentIntLogger());
        assertFalse(connectionResult.isSuccess());
        assertEquals(RestConstants.UNAUTHORIZED_401, connectionResult.getHttpStatusCode());
    }

    @Test
    public void testValidBuildTimeoutString() throws Exception {
        BlackDuckServerConfigBuilder builder = createValid();
        BlackDuckServerConfig config = builder.build();
        assertEquals(new URL(URL.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals(120, config.getTimeout());
        assertEquals(BlackDuckServerConfigBuilderTestIT.USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PASSWORD, config.getCredentials().get().getPassword().get());
    }

    @Test
    public void testValidBuildWithProxy() throws Exception {
        BlackDuckServerConfigBuilder builder = createValid();
        builder.setProxyHost(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_HOST);
        builder.setProxyPort(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_PORT);
        BlackDuckServerConfig config = builder.build();

        assertEquals(new URL(URL.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals(BlackDuckServerConfigBuilderTestIT.TIMEOUT, config.getTimeout());
        assertEquals(BlackDuckServerConfigBuilderTestIT.USERNAME, config.getCredentials().get().getUsername().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PASSWORD, config.getCredentials().get().getPassword().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_HOST, config.getProxyInfo().getHost().get());
        assertEquals(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_PORT, config.getProxyInfo().getPort());
    }

    @Test
    public void testUrlwithTrailingSlash() throws Exception {
        BlackDuckServerConfigBuilder builder = createValid();
        BlackDuckServerConfig config = builder.build();
        assertFalse(config.getBlackDuckUrl().toString().endsWith("/"));
        assertEquals("https", config.getBlackDuckUrl().url().getProtocol());
        assertEquals(new URL(URL.string()).getHost(), config.getBlackDuckUrl().url().getHost());
        assertEquals(-1, config.getBlackDuckUrl().url().getPort());
    }

    @Test
    public void testValidBuildWithProxyPortZero() {
        BlackDuckServerConfigBuilder builder = createValid();
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
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("proxy"));
        }
    }

    @Test
    public void testSolutionDetailsProvidedUserAgentString() {
        BlackDuckServerConfigBuilder builder = createValid();
        builder.setSolutionDetails(new NameVersion("Solution Test", "Test Version"));
        BlackDuckHttpClient blackDuckHttpClient = builder.build().createBlackDuckHttpClient(new SilentIntLogger());
        assertTrue(blackDuckHttpClient.getUserAgentString().startsWith("SolutionTest/TestVersion"));
    }

    @Test
    public void testSolutionDetailsOmittedProvidedUserAgentString() {
        BlackDuckServerConfigBuilder builder = createValid();
        BlackDuckHttpClient blackDuckHttpClient = builder.build().createBlackDuckHttpClient(new SilentIntLogger());
        assertTrue(blackDuckHttpClient.getUserAgentString().startsWith(BlackDuckCommon.NAME));
    }

    private BlackDuckServerConfigBuilder createValid() {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setValidDefaults(builder);
        return builder;
    }

    private BlackDuckServerConfigBuilder createBuilder() {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        setBuilderDefaults(builder);
        return builder;
    }

    private void setValidDefaults(BlackDuckServerConfigBuilder builder) {
        builder.setUrl(URL);
        builder.setUsername(BlackDuckServerConfigBuilderTestIT.USERNAME);
        builder.setPassword(BlackDuckServerConfigBuilderTestIT.PASSWORD);
        builder.setTrustCert(true);
    }

    private void setBuilderDefaults(BlackDuckServerConfigBuilder builder) {
        setValidDefaults(builder);
        builder.setTimeoutInSeconds("100");
        builder.setUsername("User");
        builder.setPassword("Pass");
    }

    private void setBuilderProxyDefaults(BlackDuckServerConfigBuilder builder) {
        builder.setProxyHost(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_HOST);
        builder.setProxyPort(BlackDuckServerConfigBuilderTestIT.PROXY_PASSTHROUGH_PORT);
    }

}
