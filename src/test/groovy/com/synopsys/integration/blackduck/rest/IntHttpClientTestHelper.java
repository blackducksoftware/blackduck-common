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

public class IntHttpClientTestHelper {
    private static Properties testProperties;

    private final String blackDuckServerUrl;

    public IntHttpClientTestHelper() {
        if (null == IntHttpClientTestHelper.testProperties) {
            initProperties();
        }
        blackDuckServerUrl = getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL);
    }

    public void initProperties() {
        Logger.getLogger(HttpClient.class.getName()).setLevel(Level.INFO);
        IntHttpClientTestHelper.testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("test.properties")) {
            IntHttpClientTestHelper.testProperties.load(is);
        } catch (Exception e) {
            System.err.println("reading test.properties failed!");
        }

        if (IntHttpClientTestHelper.testProperties.isEmpty()) {
            try {
                loadOverrideProperties(TestingPropertyKey.values());
            } catch (Exception e) {
                System.err.println("reading properties from the environment failed");
            }
        }
    }

    private void loadOverrideProperties(TestingPropertyKey[] keys) {
        for (TestingPropertyKey key : keys) {
            String prop = System.getenv(key.toString());
            if (prop != null && !prop.isEmpty()) {
                IntHttpClientTestHelper.testProperties.setProperty(key.toString(), prop);
            }
        }
    }

    public String getProperty(TestingPropertyKey key) {
        return getProperty(key.toString());
    }

    public String getProperty(String key) {
        return IntHttpClientTestHelper.testProperties.getProperty(key);
    }

    public BlackDuckServerConfig getBlackDuckServerConfig() {
        BlackDuckServerConfigBuilder builder = new BlackDuckServerConfigBuilder();
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
        return new PrintStreamIntLogger(System.out, LogLevel.INFO);
    }

    public IntLogger createIntLogger(LogLevel logLevel) {
        return new PrintStreamIntLogger(System.out, logLevel);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory() throws IllegalArgumentException, IntegrationException {
        return createBlackDuckServicesFactory(createIntLogger());
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(IntLogger logger) throws IllegalArgumentException, IntegrationException {
        BlackDuckServerConfig blackDuckServerConfig = getBlackDuckServerConfig();
        return createBlackDuckServicesFactory(blackDuckServerConfig, logger);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(BlackDuckServerConfig blackDuckServerConfig, IntLogger logger) throws IllegalArgumentException, IntegrationException {
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createCredentialsBlackDuckHttpClient(logger);
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();

        BlackDuckServicesFactory blackDuckServicesFactory = new BlackDuckServicesFactory(gson, objectMapper, blackDuckHttpClient, logger);
        return blackDuckServicesFactory;
    }

    public File getFile(String classpathResource) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
            File file = new File(url.toURI().getPath());
            return file;
        } catch (Exception e) {
            fail("Could not get file: " + e.getMessage());
            return null;
        }
    }

}
