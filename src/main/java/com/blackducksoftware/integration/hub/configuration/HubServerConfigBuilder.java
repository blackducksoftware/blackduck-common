/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.CredentialsBuilder;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.hub.rest.UriCombiner;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class HubServerConfigBuilder extends AbstractBuilder<HubServerConfig> {
    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private String hubUrl;
    private String timeoutSeconds;
    private String username;
    private String password;
    private int passwordLength;
    private String apiToken;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyNtlmDomain;
    private String proxyNtlmWorkstation;
    private int proxyPasswordLength;
    private String ignoredProxyHosts;
    private boolean alwaysTrustServerCertificate;
    private UriCombiner uriCombiner;
    private IntLogger logger;
    private final HubServerConfigValidator validator;

    public HubServerConfigBuilder() {
        this.timeoutSeconds = String.valueOf(DEFAULT_TIMEOUT_SECONDS);
        this.validator = new HubServerConfigValidator();
    }

    public HubServerConfigBuilder(final HubServerConfigValidator validator) {
        this.validator = validator;
    }

    @Override
    public HubServerConfig build() throws IllegalStateException {
        try {
            return super.build();
        } catch (final IllegalStateException stateException) {
            if (!stateException.getMessage().contains("SunCertPathBuilderException")) {
                throw stateException;
            }
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", hubUrl), stateException);
        }
    }

    @Override
    public HubServerConfig buildObject() {
        URL hubURL = null;
        try {
            String tempUrl = hubUrl;
            if (!tempUrl.endsWith("/")) {
                hubURL = new URL(tempUrl);
            } else {
                tempUrl = tempUrl.substring(0, tempUrl.length() - 1);
                hubURL = new URL(tempUrl);
            }
        } catch (final MalformedURLException e) {
        }

        final ProxyInfo proxyInfo = getHubProxyInfo();
        UriCombiner uriCombinerToUse = new UriCombiner();
        if (uriCombiner != null) {
            uriCombinerToUse = uriCombiner;
        }
        if (StringUtils.isNotBlank(apiToken)) {
            return new HubServerConfig(hubURL, NumberUtils.toInt(timeoutSeconds), apiToken, proxyInfo, alwaysTrustServerCertificate, uriCombinerToUse);
        } else {
            final Credentials credentials = getHubCredentials();
            return new HubServerConfig(hubURL, NumberUtils.toInt(timeoutSeconds), credentials, proxyInfo, alwaysTrustServerCertificate, uriCombinerToUse);
        }
    }

    private Credentials getHubCredentials() {
        final CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setPassword(password);
        credentialsBuilder.setPasswordLength(passwordLength);
        return credentialsBuilder.buildObject();
    }

    private ProxyInfo getHubProxyInfo() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        proxyBuilder.setHost(proxyHost);
        proxyBuilder.setPort(proxyPort);
        proxyBuilder.setIgnoredProxyHosts(ignoredProxyHosts);
        proxyBuilder.setUsername(proxyUsername);
        proxyBuilder.setPassword(proxyPassword);
        proxyBuilder.setPasswordLength(proxyPasswordLength);
        proxyBuilder.setNtlmDomain(proxyNtlmDomain);
        proxyBuilder.setNtlmWorkstation(proxyNtlmWorkstation);
        return proxyBuilder.buildObject();
    }

    @Override
    public AbstractValidator createValidator() {
        validator.setHubUrl(hubUrl);
        validator.setUsername(username);
        validator.setPassword(password);
        validator.setApiToken(apiToken);
        validator.setTimeout(timeoutSeconds);
        validator.setProxyHost(proxyHost);
        validator.setProxyPort(proxyPort);
        validator.setIgnoredProxyHosts(ignoredProxyHosts);
        validator.setProxyUsername(proxyUsername);
        validator.setProxyPassword(proxyPassword);
        validator.setProxyPasswordLength(proxyPasswordLength);
        validator.setAlwaysTrustServerCertificate(alwaysTrustServerCertificate);
        validator.setProxyNtlmDomain(proxyNtlmDomain);
        validator.setProxyNtlmWorkstation(proxyNtlmWorkstation);
        return validator;
    }

    public void setFromProperties(final Properties properties) {
        final String hubUrl = properties.getProperty("blackduck.hub.url");
        final String hubUsername = properties.getProperty("blackduck.hub.username");
        final String hubPassword = properties.getProperty("blackduck.hub.password");
        final String hubApiToken = properties.getProperty("blackduck.hub.api.token");
        final String hubTimeout = properties.getProperty("blackduck.hub.timeout");
        final String hubProxyHost = properties.getProperty("blackduck.hub.proxy.host");
        final String hubProxyPort = properties.getProperty("blackduck.hub.proxy.port");
        final String hubIgnoredProxyHosts = properties.getProperty("blackduck.hub.ignored.proxy.hosts");
        final String hubProxyUsername = properties.getProperty("blackduck.hub.proxy.username");
        final String hubProxyPassword = properties.getProperty("blackduck.hub.proxy.password");
        final boolean hubAlwaysTrustServerCertificate = Boolean.parseBoolean(properties.getProperty("blackduck.hub.trust.cert"));

        setHubUrl(hubUrl);
        setUsername(hubUsername);
        setPassword(hubPassword);
        setApiToken(hubApiToken);
        setTimeout(hubTimeout);
        setProxyHost(hubProxyHost);
        setProxyPort(hubProxyPort);
        setIgnoredProxyHosts(hubIgnoredProxyHosts);
        setProxyUsername(hubProxyUsername);
        setProxyPassword(hubProxyPassword);
        setAlwaysTrustServerCertificate(hubAlwaysTrustServerCertificate);
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = StringUtils.trimToNull(hubUrl);
    }

    public void setTimeout(final String timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setTimeout(final int timeoutSeconds) {
        setTimeout(String.valueOf(timeoutSeconds));
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * IMPORTANT : The password length should only be set if the password is already encrypted
     */
    public void setPasswordLength(final int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public void setApiToken(final String apiToken) {
        this.apiToken = apiToken;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(final int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
    }

    public void setProxyPort(final String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * IMPORTANT: The proxy password length should only be set if the proxy password is already encrypted
     */
    public void setProxyPasswordLength(final int proxyPasswordLength) {
        this.proxyPasswordLength = proxyPasswordLength;
    }

    public void setProxyNtlmDomain(final String proxyNtlmDomain) {
        this.proxyNtlmDomain = proxyNtlmDomain;
    }

    public void setProxyNtlmWorkstation(final String proxyNtlmWorkstation) {
        this.proxyNtlmWorkstation = proxyNtlmWorkstation;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

    public void setUriCombiner(final UriCombiner uriCombiner) {
        this.uriCombiner = uriCombiner;
    }

    public IntLogger getLogger() {
        if (logger == null) {
            logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        }
        return logger;
    }

    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }
}
