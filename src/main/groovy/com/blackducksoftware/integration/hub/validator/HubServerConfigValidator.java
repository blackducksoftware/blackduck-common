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
package com.blackducksoftware.integration.hub.validator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

import okhttp3.HttpUrl;
import okhttp3.Request;

public class HubServerConfigValidator extends AbstractValidator {
    public static final String ERROR_MSG_URL_NOT_FOUND = "No Hub Url was found.";

    public static final String ERROR_MSG_URL_NOT_VALID_PREFIX = "This is not a valid URL : ";

    public static final String ERROR_MSG_UNREACHABLE_PREFIX = "Can not reach this server : ";

    public static final String ERROR_MSG_UNREACHABLE_CAUSE = ", because: ";

    public static final String ERROR_MSG_URL_NOT_VALID = "The Hub Url is not a valid URL.";

    public static final String ERROR_MSG_URL_NOT_HUB_PREFIX = "The Url does not appear to be a Hub server :";

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

    private boolean alwaysTrustServerCertificate;

    private HubProxyInfo proxyInfo;

    @Override
    public ValidationResults assertValid() {
        final ValidationResults proxyResult = assertProxyValid();
        final ValidationResults credentialResult = assertCredentialsValid();
        final ValidationResults result = new ValidationResults();
        result.addAllResults(proxyResult.getResultMap());
        result.addAllResults(credentialResult.getResultMap());
        validateHubUrl(result);
        validateTimeout(result, null);
        return result;
    }

    public ValidationResults assertProxyValid() {
        final HubProxyValidator validator = new HubProxyValidator();
        validator.setHost(proxyHost);
        validator.setPort(proxyPort);
        validator.setIgnoredProxyHosts(ignoredProxyHosts);
        validator.setUsername(proxyUsername);
        validator.setPassword(proxyPassword);
        if (proxyPasswordLength > 0) {
            validator.setPasswordLength(proxyPasswordLength);
        }

        final ValidationResults results = validator.assertValid();
        if (results.isSuccess()) {
            if (validator.hasProxySettings()) {
                final int port = NumberUtils.toInt(proxyPort);
                if (validator.hasAuthenticatedProxySettings()) {
                    final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
                    credBuilder.setUsername(proxyUsername);
                    credBuilder.setPassword(proxyPassword);
                    credBuilder.setPasswordLength(proxyPasswordLength);
                    final HubCredentials credResult = credBuilder.build();

                    proxyInfo = new HubProxyInfo(proxyHost, port, credResult, ignoredProxyHosts);

                } else {
                    // password is blank or already encrypted so we just pass in the
                    // values given to us
                    proxyInfo = new HubProxyInfo(proxyHost, port, null, ignoredProxyHosts);
                }
            }
        }
        return results;
    }

    public ValidationResults assertCredentialsValid() {
        final HubCredentialsValidator credentialsBuilder = new HubCredentialsValidator();
        credentialsBuilder.setUsername(username);
        credentialsBuilder.setPassword(password);
        return credentialsBuilder.assertValid();
    }

    public void validateHubUrl(final ValidationResults result) {
        if (hubUrl == null) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_FOUND));
            return;
        }

        URL hubURL = null;
        try {
            hubURL = new URL(hubUrl);
            hubURL.toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_VALID));
            return;
        }

        final UnauthenticatedRestConnection restConnection = new UnauthenticatedRestConnection(new PrintStreamIntLogger(System.out, LogLevel.INFO), hubURL, NumberUtils.toInt(timeoutSeconds, 120));
        restConnection.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        if (proxyInfo != null) {
            restConnection.proxyHost = proxyInfo.getHost();
            restConnection.proxyPort = proxyInfo.getPort();
            restConnection.proxyNoHosts = proxyInfo.getIgnoredProxyHosts();
            restConnection.proxyUsername = proxyInfo.getUsername();
            try {
                restConnection.proxyPassword = proxyInfo.getDecryptedPassword();
            } catch (final Exception e) {
                result.addResult(HubProxyInfoFieldEnum.PROXYPASSWORD, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
                return;
            }
        }

        try {

            HttpUrl httpUrl = restConnection.createHttpUrl();
            Request request = restConnection.createGetRequest(httpUrl);

            try {
                restConnection.handleExecuteClientCall(request);
            } catch (final IntegrationRestException e) {
                if (e.getHttpStatusCode() == 407) {
                    result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.ERROR, e.getHttpStatusMessage()));
                } else if (e.getHttpStatusCode() != 401 && e.getHttpStatusCode() != 403) {
                    result.addResult(HubServerConfigFieldEnum.HUBURL,
                            new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + httpUrl.uri().toString() + ERROR_MSG_UNREACHABLE_CAUSE + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage()));
                }
                return;
            }
            final List<String> urlSegments = new ArrayList<>();
            urlSegments.add("download");
            urlSegments.add(CLILocation.DEFAULT_CLI_DOWNLOAD);
            httpUrl = restConnection.createHttpUrl(urlSegments);
            request = restConnection.createGetRequest(httpUrl);

            try {
                restConnection.handleExecuteClientCall(request);
            } catch (final IntegrationRestException e) {
                if (e.getHttpStatusCode() == 407) {
                    result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.ERROR, e.getHttpStatusMessage()));
                } else {
                    result.addResult(HubServerConfigFieldEnum.HUBURL,
                            new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_HUB_PREFIX + httpUrl.uri().toString() + ERROR_MSG_UNREACHABLE_CAUSE + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage()));
                }
            }

        } catch (final Exception e) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + hubUrl, e));
        }
    }

    public void validateTimeout(final ValidationResults result) {
        validateTimeout(result, null);
    }

    private void validateTimeout(final ValidationResults result, final Integer defaultTimeoutSeconds) {
        if (StringUtils.isBlank(timeoutSeconds)) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, "No Hub Timeout was found."));
            return;
        }
        int timeoutToValidate = 0;
        try {
            timeoutToValidate = stringToInteger(timeoutSeconds);
        } catch (final IllegalArgumentException e) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            return;
        }
        if (timeoutToValidate <= 0) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, "The Timeout must be greater than 0."));
        }
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
     * IMPORTANT : The password length should only be set if the password is already encrypted
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
     * IMPORTANT : The proxy password length should only be set if the proxy password is already encrypted
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

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

}
