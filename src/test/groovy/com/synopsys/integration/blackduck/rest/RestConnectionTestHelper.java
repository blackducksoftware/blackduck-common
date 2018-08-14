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
package com.synopsys.integration.blackduck.rest;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class RestConnectionTestHelper {
    private final String hubServerUrl;
    private Properties testProperties;

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
        builder.setUrl(hubServerUrl);
        builder.setUsername(getProperty(TestingPropertyKey.TEST_USERNAME));
        builder.setPassword(getProperty(TestingPropertyKey.TEST_PASSWORD));
        builder.setTimeout(getProperty(TestingPropertyKey.TEST_HUB_TIMEOUT));
        builder.setTrustCert(Boolean.parseBoolean(getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

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

    public IntLogger createIntLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.TRACE);
    }

    public IntLogger createIntLogger(final LogLevel logLevel) {
        return new PrintStreamIntLogger(System.out, logLevel);
    }

    public HubServicesFactory createHubServicesFactory() throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        return createHubServicesFactory(createIntLogger());
    }

    public HubServicesFactory createHubServicesFactory(final IntLogger logger) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        final HubServerConfig hubServerConfig = getHubServerConfig();
        return createHubServicesFactory(hubServerConfig, logger);
    }

    public HubServicesFactory createHubServicesFactory(final HubServerConfig hubServerConfig, final IntLogger logger) throws IllegalArgumentException, EncryptionException, HubIntegrationException {
        final BlackduckRestConnection restConnection = hubServerConfig.createCredentialsRestConnection(logger);

        final Gson gson = HubServicesFactory.createDefaultGson();
        final JsonParser jsonParser = HubServicesFactory.createDefaultJsonParser();
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(gson, jsonParser, restConnection, logger);
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
