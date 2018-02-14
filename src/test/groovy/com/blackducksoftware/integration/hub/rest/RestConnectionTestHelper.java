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
package com.blackducksoftware.integration.hub.rest;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class RestConnectionTestHelper {
    private Properties testProperties;

    private final String hubServerUrl;

    public RestConnectionTestHelper() {
        initProperties();
        this.hubServerUrl = getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL);
    }

    public RestConnectionTestHelper(final String hubServerUrlPropertyName) {
        initProperties();
        this.hubServerUrl = testProperties.getProperty(hubServerUrlPropertyName);
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

    public HubServerConfig getHubServerConfig() {
        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl(hubServerUrl);
        builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME));
        builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD));
        builder.setTimeout(getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT));
        builder.setAlwaysTrustServerCertificate(Boolean.parseBoolean(getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        return builder.build();
    }

    public String getIntegrationHubServerUrl() {
        return getProperty(TestingPropertyKey.TEST_HUB_SERVER_URL);
    }

    public String getTestUsername() {
        return getProperty(TestingPropertyKey.TEST_USERNAME);
    }

    public String getTestPassword() {
        return getProperty(TestingPropertyKey.TEST_PASSWORD);
    }

    public CredentialsRestConnection getIntegrationHubRestConnection() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return getRestConnection(getHubServerConfig());
    }

    public CredentialsRestConnection getRestConnection(final HubServerConfig serverConfig) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return getRestConnection(serverConfig, LogLevel.TRACE);
    }

    public CredentialsRestConnection getRestConnection(final HubServerConfig serverConfig, final LogLevel logLevel) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return serverConfig.createCredentialsRestConnection(new PrintStreamIntLogger(System.out, logLevel));
    }

    public HubServicesFactory createHubServicesFactory() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return createHubServicesFactory(LogLevel.TRACE);
    }

    public HubServicesFactory createHubServicesFactory(final LogLevel logLevel) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return createHubServicesFactory(createIntLogger(logLevel));
    }

    public IntLogger createIntLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.TRACE);
    }

    public IntLogger createIntLogger(final LogLevel logLevel) {
        return new PrintStreamIntLogger(System.out, logLevel);
    }

    public HubServicesFactory createHubServicesFactory(final IntLogger logger) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        final RestConnection restConnection = getIntegrationHubRestConnection();
        restConnection.logger = logger;
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        return hubServicesFactory;
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
