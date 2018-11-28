package com.synopsys.integration.blackduck.rest;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class RestConnectionTestHelper {
    private final String blackDuckServerUrl;
    private Properties testProperties;

    public RestConnectionTestHelper() {
        initProperties();
        blackDuckServerUrl = getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL);
    }

    public RestConnectionTestHelper(final String blackDuckServerUrlPropertyName) {
        initProperties();
        blackDuckServerUrl = testProperties.getProperty(blackDuckServerUrlPropertyName);
    }

    private void initProperties() {
        Logger.getLogger(HttpClient.class.getName()).setLevel(Level.FINE);
        testProperties = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classLoader.getResourceAsStream("test.properties")) {
            testProperties.load(is);
        } catch (final Exception e) {
            System.err.println("reading test.properties failed!");
        }

        if (testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values());
            } catch (final Exception e) {
                System.err.println("reading properties from the environment failed");
            }
        }
    }

    private void loadOverrideProperties(final TestingPropertyKey[] keys) {
        for (final TestingPropertyKey key : keys) {
            final String prop = System.getenv(key.toString());
            if (prop != null && !prop.isEmpty()) {
                testProperties.setProperty(key.toString(), prop);
            }
        }
    }

    public String getProperty(final TestingPropertyKey key) {
        return getProperty(key.toString());
    }

    public String getProperty(final String key) {
        return testProperties.getProperty(key);
    }

    public BlackDuckServerConfig getBlackDuckServerConfig() {
        final BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
        builder.setUrl(blackDuckServerUrl);
        builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME));
        builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD));
        builder.setTimeout(getProperty(TestingPropertyKey.TEST_BLACK_DUCK_TIMEOUT));
        builder.setTrustCert(Boolean.parseBoolean(getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        return builder.build();
    }

    public String getIntegrationBlackDuckServerUrl() {
        return getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL);
    }

    public String getTestUsername() {
        return getProperty(TestingPropertyKey.TEST_USERNAME);
    }

    public String getTestPassword() {
        return getProperty(TestingPropertyKey.TEST_PASSWORD);
    }

    public IntLogger createIntLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.TRACE);
    }

    public IntLogger createIntLogger(final LogLevel logLevel) {
        return new PrintStreamIntLogger(System.out, logLevel);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory() throws IllegalArgumentException, IntegrationException {
        return createBlackDuckServicesFactory(createIntLogger());
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final IntLogger logger) throws IllegalArgumentException, IntegrationException {
        final BlackDuckServerConfig blackDuckServerConfig = getBlackDuckServerConfig();
        return createBlackDuckServicesFactory(blackDuckServerConfig, logger);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final BlackDuckServerConfig blackDuckServerConfig, final IntLogger logger) throws IllegalArgumentException, IntegrationException {
        final BlackDuckRestConnection restConnection = blackDuckServerConfig.createCredentialsRestConnection(logger);
        final Gson gson = BlackDuckServicesFactory.createDefaultGson();
        final ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();

        final BlackDuckServicesFactory blackDuckServicesFactory = new BlackDuckServicesFactory(gson, objectMapper, restConnection, logger);
        return blackDuckServicesFactory;
    }

    public File getFile(final String classpathResource) {
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
            final File file = new File(url.toURI().getPath());
            return file;
        } catch (final Exception e) {
            fail("Could not get file: " + e.getMessage());
            return null;
        }
    }

}
