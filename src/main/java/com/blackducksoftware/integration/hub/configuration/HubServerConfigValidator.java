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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.ApiTokenField;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.CredentialsBuilder;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoField;
import com.blackducksoftware.integration.hub.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.hub.service.model.HubServerVerifier;
import com.blackducksoftware.integration.hub.validator.CredentialsValidator;
import com.blackducksoftware.integration.hub.validator.ProxyInfoValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

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
    private String apiToken;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private int proxyPasswordLength;
    private String ignoredProxyHosts;
    private String ntlmDomain;
    private String ntlmWorkstation;

    private boolean alwaysTrustServerCertificate;
    private ProxyInfo proxyInfo;

    @Override
    public ValidationResults assertValid() {
        final ValidationResults proxyResult = assertProxyValid();
        final ValidationResults credentialsOrApiTokenResult = assertCredentialsOrApiTokenValid();
        final ValidationResults result = new ValidationResults();
        result.addAllResults(proxyResult.getResultMap());
        result.addAllResults(credentialsOrApiTokenResult.getResultMap());
        validateHubUrl(result);
        validateTimeout(result, null);
        return result;
    }

    public ValidationResults assertProxyValid() {
        final ProxyInfoValidator validator = new ProxyInfoValidator();
        validator.setHost(proxyHost);
        validator.setPort(proxyPort);
        validator.setIgnoredProxyHosts(ignoredProxyHosts);
        validator.setUsername(proxyUsername);
        validator.setPassword(proxyPassword);
        validator.setNtlmDomain(ntlmDomain);
        validator.setNtlmWorkstation(ntlmWorkstation);
        if (proxyPasswordLength > 0) {
            validator.setPasswordLength(proxyPasswordLength);
        }

        final ValidationResults results = validator.assertValid();
        if (results.isSuccess()) {
            if (validator.hasProxySettings()) {
                final int port = NumberUtils.toInt(proxyPort);
                if (validator.hasAuthenticatedProxySettings()) {
                    final CredentialsBuilder credBuilder = new CredentialsBuilder();
                    credBuilder.setUsername(proxyUsername);
                    credBuilder.setPassword(proxyPassword);
                    credBuilder.setPasswordLength(proxyPasswordLength);
                    final Credentials credResult = credBuilder.build();

                    proxyInfo = new ProxyInfo(proxyHost, port, credResult, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);

                } else {
                    // password is blank or already encrypted so we just pass in the
                    // values given to us
                    proxyInfo = new ProxyInfo(proxyHost, port, null, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
                }
            }
        }
        return results;
    }

    // you can specify either username/password OR apiToken
    public ValidationResults assertCredentialsOrApiTokenValid() {
        final ValidationResults validationResults = new ValidationResults();

        if (StringUtils.isBlank(apiToken)) {
            final CredentialsValidator credentialsBuilder = new CredentialsValidator();
            credentialsBuilder.setUsername(username);
            credentialsBuilder.setPassword(password);
            final ValidationResults credentialsResults = credentialsBuilder.assertValid();
            validationResults.addAllResults(credentialsResults.getResultMap());

            if (validationResults.hasErrors()) {
                validationResults.addResult(ApiTokenField.API_TOKEN, new ValidationResult(ValidationResultEnum.ERROR, "No api token was found."));
            }
        }

        return validationResults;
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

        try {
            final HubServerVerifier hubServerVerifier = new HubServerVerifier(hubURL, proxyInfo, alwaysTrustServerCertificate, NumberUtils.toInt(timeoutSeconds, 120));
            hubServerVerifier.verifyIsHubServer();

        } catch (final IntegrationRestException e) {
            if (e.getHttpStatusCode() == 407) {
                result.addResult(ProxyInfoField.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.ERROR, e.getHttpStatusMessage()));
            } else {
                result.addResult(HubServerConfigFieldEnum.HUBURL,
                        new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + hubUrl + ERROR_MSG_UNREACHABLE_CAUSE + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e));
            }
        } catch (final IntegrationException e) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + hubUrl + ERROR_MSG_UNREACHABLE_CAUSE + e.getMessage(), e));
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

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(final String apiToken) {
        this.apiToken = apiToken;
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
