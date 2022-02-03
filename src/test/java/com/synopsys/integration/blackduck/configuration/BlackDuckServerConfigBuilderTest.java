package com.synopsys.integration.blackduck.configuration;

import static com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigKeys.KEYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.http.client.CookieHeaderParser;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.builder.BuilderProperties;
import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BlackDuckServerConfigBuilderTest {
    public static final String API_TOKEN = "a valid API_TOKEN_KEY";
    private static final String GOOD_URL = "https://blackduck-common.com";
    private static final String USERNAME = "a valid USERNAME_KEY";
    private static final String PASSWORD = "a valid PASSWORD_KEY";
    private static final String VALUE = "a valid property value";
    private static final String SOLUTION_NAME = "a valid SOLUTION_NAME_KEY";
    private static final String SOLUTION_VERSION = "a valid SOLUTION_VERSION_KEY";
    public static final int TIMEOUT = 60;

    @Test
    public void testSettingFromPropertiesMapWithMixed() {
        Map<String, String> properties = new HashMap<>();
        properties.put("BLACKDUCK_URL", "test url");
        properties.put("blackduck.username", USERNAME);
        properties.put("BLACKDUCK_PASSWORD", PASSWORD);

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        assertNull(blackDuckServerConfigBuilder.getUrl());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());

        blackDuckServerConfigBuilder.setProperties(properties.entrySet());

        assertEquals("test url", blackDuckServerConfigBuilder.getUrl());
        assertEquals(USERNAME, blackDuckServerConfigBuilder.getUsername());
        assertEquals(PASSWORD, blackDuckServerConfigBuilder.getPassword());
    }

    @Test
    public void testValidateBlackDuckURLEmpty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertTrue(builderStatus.getFullErrorMessage().contains("The Black Duck url must be specified."));
    }

    @Test
    public void testNullUrlInvalid() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl((String) null);

        assertFalse(blackDuckServerConfigBuilder.isValid());
        try {
            blackDuckServerConfigBuilder.build();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testValidateBlackDuckURLMalformed() {
        String blackDuckUrl = "TestString";
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertTrue(builderStatus.getFullErrorMessage().contains("The provided Black Duck url (TestString) is not a valid URL."));
    }

    @Test
    public void testValidateBlackDuckURLMalformed2() {
        String blackDuckUrl = "http:TestString";
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);

        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testZeroTimeout() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL)
            .setApiToken(API_TOKEN)
            .setTimeoutInSeconds(0);
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("The timeout must be greater than zero.", builderStatus.getFullErrorMessage());
    }

    @Test
    public void testPopulatingExecutorService() throws Exception {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newSingleThreadExecutor();

            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
            blackDuckServerConfigBuilder.setUrl(GOOD_URL);
            blackDuckServerConfigBuilder.setApiToken(API_TOKEN);
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();

            Field executorServiceField = BlackDuckServerConfig.class.getDeclaredField("executorService");
            executorServiceField.setAccessible(true);
            assertTrue(executorServiceField.get(blackDuckServerConfig) instanceof NoThreadExecutorService);

            assertNotNull(blackDuckServerConfigBuilder.getExecutorService());
            blackDuckServerConfigBuilder.setExecutorService(executorService);
            assertEquals(executorService, blackDuckServerConfigBuilder.getExecutorService().orElse(null));
            blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            assertNotNull(executorServiceField.get(blackDuckServerConfig));
        } finally {
            assert executorService != null;
            executorService.shutdownNow();
        }
    }

    //HERE
    @Test
    public void testNewBuilderIncludesAllKeys() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();
        blackDuckServerConfigBuilder.setApiToken(API_TOKEN)
            .setUrl(GOOD_URL);
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertTrue(builderStatus.isValid());
        assertTrue(StringUtils.isEmpty(builderStatus.getFullErrorMessage()));
        assertEquals(API_TOKEN, blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testInvalidNewBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("Configured to use API Token but token was not set.", builderStatus.getFullErrorMessage());
        assertTrue(StringUtils.isBlank(blackDuckServerConfigBuilder.getApiToken()));
    }

    @Test
    public void testApiBuilderTokenSet() throws IntegrationException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        HttpUrl httpUrl = new HttpUrl(GOOD_URL);
        blackDuckServerConfigBuilder.setApiToken(API_TOKEN)
            .setUrl(httpUrl);
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertTrue(builderStatus.isValid());
        assertTrue(StringUtils.isEmpty(builderStatus.getFullErrorMessage()));
        assertEquals(API_TOKEN, blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testInvalidApiTokenBuilder() throws IntegrationException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        HttpUrl httpUrl = new HttpUrl(GOOD_URL);
        blackDuckServerConfigBuilder.setUrl(httpUrl);
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("Configured to use API Token but token was not set.", builderStatus.getFullErrorMessage());
        assertTrue(StringUtils.isBlank(blackDuckServerConfigBuilder.getApiToken()));
    }

    @Test
    public void testNoCredentialsWithApiToken() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();

        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.empty();
        intEnvironmentVariables.put(BlackDuckServerConfigBuilder.URL_KEY.getKey(), GOOD_URL);
        blackDuckServerConfigBuilder.setProperties(intEnvironmentVariables.getVariables().entrySet());
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("Configured to use API Token but token was not set.", builderStatus.getFullErrorMessage());
    }

    @Test
    public void testCredentialsBuilderCredentialsSet() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUsername(USERNAME)
            .setPassword(PASSWORD)
            .setUrl(GOOD_URL);
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertTrue(builderStatus.isValid());
        assertTrue(StringUtils.isEmpty(builderStatus.getFullErrorMessage()));
        assertEquals(USERNAME, blackDuckServerConfigBuilder.getUsername());
        assertEquals(PASSWORD, blackDuckServerConfigBuilder.getPassword());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testInvalidCredentialsBuilderNoCredentials() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("Configured to use username/password but both are not valid.", builderStatus.getFullErrorMessage());
        assertTrue(StringUtils.isBlank(blackDuckServerConfigBuilder.getApiToken()));
    }

    @Test
    public void testInvalidCredentialsBuilderWithNulls() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("", null);
        blackDuckServerConfigBuilder.setCredentials(credentialsBuilder.build());
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testConstructorOnlyCommonKeys() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertFalse(builderStatus.isValid());
        assertEquals("Must be configured to use an API Token or username/password.", builderStatus.getFullErrorMessage());
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testProxyInfoMethods() {
        ProxyInfo proxyInfo = getValidProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setProxyInfo(proxyInfo);

        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(proxyInfo.getHost().orElse(null), blackDuckServerConfigBuilder.getProxyHost());
        assertEquals(proxyInfo.getPort(), blackDuckServerConfigBuilder.getProxyPort());
        assertEquals(proxyInfo.getUsername().orElse(null), blackDuckServerConfigBuilder.getProxyUsername());
        assertEquals(proxyInfo.getPassword().orElse(null), blackDuckServerConfigBuilder.getProxyPassword());
        assertEquals(proxyInfo.getNtlmDomain().orElse(null), blackDuckServerConfigBuilder.getProxyNtlmDomain());
        assertEquals(proxyInfo.getNtlmWorkstation().orElse(null), blackDuckServerConfigBuilder.getProxyNtlmWorkstation());
    }

    @Test
    public void testNullCheckSetters() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();

        assertNotNull(blackDuckServerConfigBuilder.getLogger());
        assertNotNull(blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertNotNull(blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertNotNull(blackDuckServerConfigBuilder.getGson());
        assertNotNull(blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertNotNull(blackDuckServerConfigBuilder.getObjectMapper());

        blackDuckServerConfigBuilder.setLogger(null)
            .setAuthenticationSupport(null)
            .setCookieHeaderParser(null)
            .setGson(null)
            .setIntEnvironmentVariables(null)
            .setObjectMapper(null);

        assertNotNull(blackDuckServerConfigBuilder.getLogger());
        assertNotNull(blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertNotNull(blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertNotNull(blackDuckServerConfigBuilder.getGson());
        assertNotNull(blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertNotNull(blackDuckServerConfigBuilder.getObjectMapper());

        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        AuthenticationSupport authenticationSupport = new AuthenticationSupport();
        CookieHeaderParser cookieHeaderParser = new CookieHeaderParser();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.empty();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();

        blackDuckServerConfigBuilder.setLogger(logger)
            .setAuthenticationSupport(authenticationSupport)
            .setCookieHeaderParser(cookieHeaderParser)
            .setGson(gson)
            .setIntEnvironmentVariables(intEnvironmentVariables)
            .setObjectMapper(objectMapper);

        assertEquals(logger, blackDuckServerConfigBuilder.getLogger());
        assertEquals(authenticationSupport, blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertEquals(cookieHeaderParser, blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertEquals(gson, blackDuckServerConfigBuilder.getGson());
        assertEquals(intEnvironmentVariables, blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertEquals(objectMapper, blackDuckServerConfigBuilder.getObjectMapper());
    }

    @Test
    public void testTrustCertString() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL)
            .setApiToken(API_TOKEN)
            .setTrustCert("true");

        assertTrue(blackDuckServerConfigBuilder.isTrustCert());
    }

    @Test
    public void testTrustCertBoolean() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL)
            .setApiToken(API_TOKEN)
            .setTrustCert(false);

        assertFalse(blackDuckServerConfigBuilder.isTrustCert());
    }

    @Test
    public void testTrustCertString2() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL)
            .setApiToken(API_TOKEN)
            .setTrustCert("junk");

        assertFalse(blackDuckServerConfigBuilder.isTrustCert());
    }

    @Test
    public void testSetPropertiesValidProperty() {
        Map<String, String> properties = new HashMap<>();
        properties.put(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY.getKey(), VALUE);

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setProperties(properties.entrySet());

        assertTrue(blackDuckServerConfigBuilder.getProperties().containsKey(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY));
        assertEquals(VALUE, blackDuckServerConfigBuilder.getProperties().get(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY));
    }

    @Test
    public void testSetPropertiesInValidProperty() {
        Map<String, String> properties = new HashMap<>();
        properties.put("junk", VALUE);
        BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey("junk");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setProperties(properties.entrySet());

        assertFalse(blackDuckServerConfigBuilder.getProperties().containsKey(builderPropertyKey));
    }

    @Test
    public void testSetPropertyValidProperty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setProperty(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY.getKey(), VALUE);

        assertTrue(blackDuckServerConfigBuilder.getProperties().containsKey(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY));
        assertEquals(VALUE, blackDuckServerConfigBuilder.getProperties().get(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY));
    }

    @Test
    public void testSetPropertyInValidProperty() {
        BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey("junk");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setProperty("junk", VALUE);

        assertFalse(blackDuckServerConfigBuilder.getProperties().containsKey(builderPropertyKey));
    }

    @Test
    public void testGetPropertyKeys() {
        BuilderProperties apiTokenBuilderProperties = new BuilderProperties(KEYS.apiToken);
        Set<String> expectedPropertyKeys = apiTokenBuilderProperties.getPropertyKeys();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        Set<String> configBuilderPropertyKeys = blackDuckServerConfigBuilder.getPropertyKeys();

        assertEquals(expectedPropertyKeys, configBuilderPropertyKeys);
    }

    @Test
    public void testGetEnvironmentVariableKeys() {
        BuilderProperties apiTokenBuilderProperties = new BuilderProperties(KEYS.apiToken);
        String apiTokenEnvVarKey = apiTokenBuilderProperties.getEnvironmentVariableKeys().iterator().next();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        Set<String> configBuilderPropertyKeys = blackDuckServerConfigBuilder.getEnvironmentVariableKeys();

        assertTrue(configBuilderPropertyKeys.contains(apiTokenEnvVarKey));
    }

    @Test
    public void testSetSolutionDetailsStrings() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setSolutionDetails(SOLUTION_NAME, SOLUTION_VERSION);

        assertEquals(SOLUTION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
    }

    @Test
    public void testSetSolutionDetailsNameVersion() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        NameVersion nameVersion = new NameVersion(SOLUTION_NAME, SOLUTION_VERSION);
        blackDuckServerConfigBuilder.setSolutionDetails(nameVersion);

        assertEquals(SOLUTION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
    }

    @Test
    public void testBuildWithoutValidationNoValues() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl("");

        assertTrue(StringUtils.isBlank(blackDuckServerConfigBuilder.getUrl()));
        assertNull(blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertNull(blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());
        assertNull(blackDuckServerConfigBuilder.getApiToken());
        assertTrue(blackDuckServerConfigBuilder.getProxyInfo().isBlank());
        assertEquals(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS, blackDuckServerConfigBuilder.getTimeoutInSeconds());
        assertFalse(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertNull(blackDuckServerConfig.getBlackDuckUrl());
        assertNull(blackDuckServerConfig.getSolutionDetails().getName());
        assertNull(blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials().flatMap(Credentials::getUsername));
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials().flatMap(Credentials::getPassword));
        assertEquals(Optional.empty(), blackDuckServerConfig.getApiToken());
        assertTrue(blackDuckServerConfig.getProxyInfo().isBlank());
        assertEquals(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS, blackDuckServerConfig.getTimeout());
        assertFalse(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testBuildWithoutValidationApiToken() {
        ProxyInfo proxyInfo = getValidProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();

        blackDuckServerConfigBuilder.setApiToken(API_TOKEN)
            .setUrl(GOOD_URL)
            .setSolutionDetails(SOLUTION_NAME, SOLUTION_VERSION)
            .setProxyInfo(proxyInfo)
            .setTimeoutInSeconds(TIMEOUT)
            .setTrustCert(true);

        assertEquals(API_TOKEN, blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertEquals(SOLUTION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());
        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfigBuilder.getTimeoutInSeconds());
        assertTrue(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertTrue(blackDuckServerConfig.usingApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfig.getBlackDuckUrl().string());
        assertEquals(SOLUTION_NAME, blackDuckServerConfig.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials());
        assertEquals(API_TOKEN, blackDuckServerConfig.getApiToken().orElse(null));
        assertEquals(proxyInfo, blackDuckServerConfig.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfig.getTimeout());
        assertTrue(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testBuildWithoutValidationCredentials() throws IntegrationException {
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(USERNAME, PASSWORD);
        Credentials credentials = credentialsBuilder.build();
        NameVersion nameVersion = new NameVersion(SOLUTION_NAME, SOLUTION_VERSION);
        HttpUrl httpUrl = new HttpUrl(GOOD_URL);
        ProxyInfo proxyInfo = getValidProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();

        blackDuckServerConfigBuilder.setCredentials(credentials)
            .setSolutionDetails(nameVersion)
            .setUrl(httpUrl)
            .setProxyInfo(proxyInfo)
            .setTimeoutInSeconds(TIMEOUT)
            .setTrustCert("false");

        assertNull(blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertEquals(SOLUTION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertEquals(USERNAME, blackDuckServerConfigBuilder.getUsername());
        assertEquals(PASSWORD, blackDuckServerConfigBuilder.getPassword());
        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfigBuilder.getTimeoutInSeconds());
        assertFalse(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertFalse(blackDuckServerConfig.usingApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfig.getBlackDuckUrl().string());
        assertEquals(SOLUTION_NAME, blackDuckServerConfig.getSolutionDetails().getName());
        assertEquals(SOLUTION_VERSION, blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(USERNAME, blackDuckServerConfig.getCredentials().flatMap(Credentials::getUsername).orElse(null));
        assertEquals(PASSWORD, blackDuckServerConfig.getCredentials().flatMap(Credentials::getPassword).orElse(null));
        assertNull(blackDuckServerConfig.getApiToken().orElse(null));
        assertEquals(proxyInfo, blackDuckServerConfig.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfig.getTimeout());
        assertFalse(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testValidateBlankBDUrl() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setApiToken(API_TOKEN);

        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertEquals("The Black Duck url must be specified.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBadBDCredentials() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newCredentialsBuilder();
        blackDuckServerConfigBuilder.setUsername(USERNAME)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertEquals("The username and password must both be populated or both be empty.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBlankApiToken() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);

        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertEquals("Configured to use API Token but token was not set.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBadProxyCredentials() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setProxyUsername(USERNAME)
            .setApiToken(API_TOKEN)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = blackDuckServerConfigBuilder.validateAndGetBuilderStatus();

        assertTrue(builderStatus.getFullErrorMessage().contains("The proxy credentials were not valid."));
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBlankProxyHost() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiTokenBuilder();
        blackDuckServerConfigBuilder.setProxyUsername(USERNAME)
            .setProxyPassword(PASSWORD)
            .setApiToken(API_TOKEN)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertEquals("The proxy host must be specified and the port must be greater than zero.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    private ProxyInfo getValidProxyInfo() {
        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost("proxy-host");
        proxyInfoBuilder.setPort(1234);
        proxyInfoBuilder.setNtlmDomain("proxy-ntlmDomain");
        proxyInfoBuilder.setNtlmWorkstation("proxy-ntlmWorkstation");

        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("proxy-username", "proxy-password");
        proxyInfoBuilder.setCredentials(credentialsBuilder.build());

        return proxyInfoBuilder.build();
    }

}
