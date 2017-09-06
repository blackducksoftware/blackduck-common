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
package com.blackducksoftware.integration.hub.builder;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.hub.certificate.HubCertificateHandler;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.validator.HubServerConfigValidator;
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

    private String proxyHost;

    private String proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private int proxyPasswordLength;

    private String ignoredProxyHosts;

    private boolean autoImportHttpsCertificates;

    private IntLogger logger;

    public HubServerConfigBuilder() {
        timeoutSeconds = String.valueOf(DEFAULT_TIMEOUT_SECONDS);
    }

    @Override
    public HubServerConfig build() throws IllegalStateException {
        try {
            return super.build();
        } catch (final IllegalStateException stateException) {
            if (!stateException.getMessage().contains("SunCertPathBuilderException")) {
                throw stateException;
            }

            if (autoImportHttpsCertificates) {
                final HubCertificateHandler hubCertificateHandler = new HubCertificateHandler(getLogger());
                final int timeout = NumberUtils.toInt(timeoutSeconds, DEFAULT_TIMEOUT_SECONDS);
                hubCertificateHandler.setTimeout(timeout);
                try {
                    final URL url = new URL(hubUrl);
                    if (getHubProxyInfo().getProxy(url) != Proxy.NO_PROXY) {
                        hubCertificateHandler.setProxyHost(proxyHost);
                        hubCertificateHandler.setProxyPort(NumberUtils.toInt(proxyPort));
                        hubCertificateHandler.setProxyUsername(proxyUsername);
                        hubCertificateHandler.setProxyPassword(proxyPassword);
                    }
                    hubCertificateHandler.importHttpsCertificateForHubServer(url);
                    return super.build();
                } catch (final Exception certificateException) {
                    throw new IntegrationCertificateException(certificateException.getMessage());
                }
            }

            // In case of proxy, or if autoImportHttpsCertificates == false we
            // wont attempt to import certificates. The
            // User will have to do it on their own.
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

        final HubCredentials credentials = getHubCredentials();
        final HubProxyInfo proxyInfo = getHubProxyInfo();
        final HubServerConfig config = new HubServerConfig(hubURL, NumberUtils.toInt(timeoutSeconds), credentials, proxyInfo, autoImportHttpsCertificates);
        return config;
    }

    private HubCredentials getHubCredentials() {
        final HubCredentialsBuilder credentialsBuilder = new HubCredentialsBuilder();
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setPassword(password);
        credentialsBuilder.setPasswordLength(passwordLength);
        return credentialsBuilder.buildObject();
    }

    private HubProxyInfo getHubProxyInfo() {
        final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
        proxyBuilder.setHost(proxyHost);
        proxyBuilder.setPort(proxyPort);
        proxyBuilder.setIgnoredProxyHosts(ignoredProxyHosts);
        proxyBuilder.setUsername(proxyUsername);
        proxyBuilder.setPassword(proxyPassword);
        proxyBuilder.setPasswordLength(proxyPasswordLength);
        return proxyBuilder.buildObject();
    }

    @Override
    public AbstractValidator createValidator() {
        final HubServerConfigValidator validator = new HubServerConfigValidator();
        validator.setHubUrl(hubUrl);
        validator.setUsername(username);
        validator.setPassword(password);
        validator.setTimeout(timeoutSeconds);
        validator.setProxyHost(proxyHost);
        validator.setProxyPort(proxyPort);
        validator.setIgnoredProxyHosts(ignoredProxyHosts);
        validator.setProxyUsername(proxyUsername);
        validator.setProxyPassword(proxyPassword);
        validator.setProxyPasswordLength(proxyPasswordLength);
        return validator;
    }

    public void setFromProperties(final Properties properties) {
        final String hubUrl = properties.getProperty("hub.url");
        final String hubUsername = properties.getProperty("hub.username");
        final String hubPassword = properties.getProperty("hub.password");
        final String hubTimeout = properties.getProperty("hub.timeout");
        final String hubProxyHost = properties.getProperty("hub.proxy.host");
        final String hubProxyPort = properties.getProperty("hub.proxy.port");
        final String hubIgnoredProxyHosts = properties.getProperty("hub.ignored.proxy.hosts");
        final String hubProxyUsername = properties.getProperty("hub.proxy.username");
        final String hubProxyPassword = properties.getProperty("hub.proxy.password");

        setHubUrl(hubUrl);
        setUsername(hubUsername);
        setPassword(hubPassword);
        setTimeout(hubTimeout);
        setProxyHost(hubProxyHost);
        setProxyPort(hubProxyPort);
        setIgnoredProxyHosts(hubIgnoredProxyHosts);
        setProxyUsername(hubProxyUsername);
        setProxyPassword(hubProxyPassword);
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
     * IMPORTANT : The proxy password length should only be set if the proxy password is already encrypted
     */
    public void setProxyPasswordLength(final int proxyPasswordLength) {
        this.proxyPasswordLength = proxyPasswordLength;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public void setAutoImportHttpsCertificates(final boolean autoImportHttpsCertificates) {
        this.autoImportHttpsCertificates = autoImportHttpsCertificates;
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
