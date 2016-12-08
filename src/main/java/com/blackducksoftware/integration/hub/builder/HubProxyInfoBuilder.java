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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.validator.HubProxyValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class HubProxyInfoBuilder extends AbstractBuilder<HubProxyInfo> {
    private String host;

    private String port;

    private String username;

    private String password;

    private int passwordLength;

    private String ignoredProxyHosts;

    @Override
    public HubProxyInfo buildObject() throws IllegalArgumentException {
        HubProxyInfo proxyInfo = null;
        final int proxyPort = NumberUtils.toInt(port);
        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(username)) {

            final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
            credBuilder.setUsername(username);
            credBuilder.setPassword(password);
            credBuilder.setPasswordLength(passwordLength);
            final HubCredentials credResult = credBuilder.build();

            proxyInfo = new HubProxyInfo(host, proxyPort, credResult, ignoredProxyHosts);
        } else {
            // password is blank or already encrypted so we just pass in the
            // values given to us
            proxyInfo = new HubProxyInfo(host, proxyPort, null, ignoredProxyHosts);
        }

        return proxyInfo;
    }

    @Override
    public AbstractValidator createValidator() {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(getHost());
        validator.setPort(getPort());
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        validator.setPasswordLength(getPasswordLength());
        validator.setIgnoredProxyHosts(getIgnoredProxyHosts());
        return validator;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final int port) {
        setPort(String.valueOf(port));
    }

    public void setPort(final String port) {
        this.port = port;
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

    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public boolean hasProxySettings() {
        return StringUtils.isNotBlank(host) || StringUtils.isNotBlank(port) || StringUtils.isNotBlank(username) || StringUtils.isNotBlank(password)
                || StringUtils.isNotBlank(ignoredProxyHosts);
    }
}
