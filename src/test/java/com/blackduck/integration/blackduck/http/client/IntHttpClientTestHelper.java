package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.util.IntEnvironmentVariables;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

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
        return getBlackDuckServerConfigBuilder().build();
    }

    public BlackDuckServerConfigBuilder getBlackDuckServerConfigBuilder() {
        BlackDuckServerConfigBuilder builder;

        String apiToken = getProperty(TestingPropertyKey.TEST_API_TOKEN);
        if (StringUtils.isNotBlank(apiToken)) {
            builder = BlackDuckServerConfig.newApiTokenBuilder();
            builder.setApiToken(apiToken);
        } else {
            builder = BlackDuckServerConfig.newCredentialsBuilder();
            builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME));
            builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD));
        }

        builder.setUrl(blackDuckServerUrl)
            .setTimeoutInSeconds(getProperty(TestingPropertyKey.TEST_BLACK_DUCK_TIMEOUT))
            .setTrustCert(Boolean.parseBoolean(getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        return builder;
    }

    public HttpUrl getIntegrationBlackDuckServerUrl() {
        try {
            return new HttpUrl(getProperty(TestingPropertyKey.TEST_BLACK_DUCK_SERVER_URL));
        } catch (IntegrationException e) {
            throw new RuntimeException(String.format("Your test url is bad: %s", e.getMessage()), e);
        }
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
        BlackDuckHttpClient blackDuckHttpClient = blackDuckServerConfig.createBlackDuckHttpClient(logger);
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();
        ExecutorService executorService = BlackDuckServicesFactory.NO_THREAD_EXECUTOR_SERVICE;

        BlackDuckServicesFactory blackDuckServicesFactory = new BlackDuckServicesFactory(intEnvironmentVariables, executorService, logger, blackDuckHttpClient);
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

    public void deleteIfProjectExists(IntLogger logger, ProjectService projectService, BlackDuckApiClient blackDuckApiClient, String projectName) throws Exception {
        try {
            Optional<ProjectView> project = projectService.getProjectByName(projectName);
            if (project.isPresent()) {
                blackDuckApiClient.delete(project.get());
            }
        } catch (BlackDuckIntegrationException e) {
            logger.warn("Project didn't exist");
        }
    }

}
