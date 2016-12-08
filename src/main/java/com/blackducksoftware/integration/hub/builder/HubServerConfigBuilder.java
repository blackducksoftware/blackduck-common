/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.validator.HubServerConfigValidator;
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

        final HubCredentialsBuilder credentialsBuilder = new HubCredentialsBuilder();
        credentialsBuilder.setUsername(getUsername());
        credentialsBuilder.setPassword(getPassword());
        credentialsBuilder.setPasswordLength(getPasswordLength());
        final HubCredentials credentials = credentialsBuilder.build();

        final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
        proxyBuilder.setHost(getProxyHost());
        proxyBuilder.setPort(getProxyPort());
        proxyBuilder.setIgnoredProxyHosts(getIgnoredProxyHosts());
        proxyBuilder.setUsername(getProxyUsername());
        proxyBuilder.setPassword(getProxyPassword());
        proxyBuilder.setPasswordLength(getProxyPasswordLength());
        final HubProxyInfo proxyInfo = proxyBuilder.build();
        final HubServerConfig config = new HubServerConfig(hubURL, NumberUtils.toInt(timeoutSeconds), credentials, proxyInfo);
        return config;
    }

    @Override
    public AbstractValidator createValidator() {
        final HubServerConfigValidator validator = new HubServerConfigValidator();
        validator.setHubUrl(getHubUrl());
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        validator.setTimeout(getTimeout());
        validator.setProxyHost(getProxyHost());
        validator.setProxyPort(getProxyPort());
        validator.setIgnoredProxyHosts(getIgnoredProxyHosts());
        validator.setProxyUsername(getProxyUsername());
        validator.setProxyPassword(getProxyPassword());
        validator.setProxyPasswordLength(getProxyPasswordLength());
        return validator;
    }

    public void setFromProperties(Properties properties) {
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

    public String getTimeout() {
        return timeoutSeconds;
    }

    public void setTimeout(final int timeoutSeconds) {
        setTimeout(String.valueOf(timeoutSeconds));
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    /**
     * IMPORTANT : The password length should only be set if the password is
     * already encrypted
     */
    public void setPasswordLength(final int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
    }

    public void setProxyPort(final String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public int getProxyPasswordLength() {
        return proxyPasswordLength;
    }

    /**
     * IMPORTANT : The proxy password length should only be set if the proxy
     * password is already encrypted
     */
    public void setProxyPasswordLength(final int proxyPasswordLength) {
        this.proxyPasswordLength = proxyPasswordLength;
    }

    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }
}
