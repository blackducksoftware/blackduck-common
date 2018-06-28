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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.credentials.CredentialsBuilder;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class HubServerConfigBuilder extends AbstractBuilder<HubServerConfig> {
    public static final String HUB_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX = "BLACKDUCK_HUB_";
    public static final String HUB_SERVER_CONFIG_PROPERTY_KEY_PREFIX = "blackduck.hub.";

    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private final HubServerConfigValidator validator;
    private final Map<Property, String> values = new HashMap<>();
    private IntLogger logger;

    public HubServerConfigBuilder() {
        this(new HubServerConfigValidator());
    }

    public HubServerConfigBuilder(final HubServerConfigValidator validator) {
        this.validator = validator;
        EnumSet.allOf(Property.class).forEach(property -> {
            values.put(property, null);
        });
        values.put(Property.TIMEOUT, String.valueOf(DEFAULT_TIMEOUT_SECONDS));
    }

    @Override
    public HubServerConfig build() throws IllegalStateException {
        try {
            return super.build();
        } catch (final IllegalStateException stateException) {
            if (!stateException.getMessage().contains("SunCertPathBuilderException")) {
                throw stateException;
            }
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", url()), stateException);
        }
    }

    @Override
    public HubServerConfig buildObject() {
        URL hubURL = null;
        try {
            String tempUrl = url();
            if (!tempUrl.endsWith("/")) {
                hubURL = new URL(tempUrl);
            } else {
                tempUrl = tempUrl.substring(0, tempUrl.length() - 1);
                hubURL = new URL(tempUrl);
            }
        } catch (final MalformedURLException e) {
        }

        final ProxyInfo proxyInfo = getHubProxyInfo();
        if (StringUtils.isNotBlank(apiToken())) {
            return new HubServerConfig(hubURL, timeoutSeconds(), apiToken(), proxyInfo, trustCert());
        } else {
            final Credentials credentials = getHubCredentials();
            return new HubServerConfig(hubURL, timeoutSeconds(), credentials, proxyInfo, trustCert());
        }
    }

    private Credentials getHubCredentials() {
        final CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        credentialsBuilder.setUsername(values.get(Property.USERNAME));
        credentialsBuilder.setPassword(values.get(Property.PASSWORD));
        credentialsBuilder.setPasswordLength(passwordLength());
        return credentialsBuilder.buildObject();
    }

    private ProxyInfo getHubProxyInfo() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        proxyBuilder.setHost(values.get(Property.PROXY_HOST));
        proxyBuilder.setPort(values.get(Property.PROXY_PORT));
        proxyBuilder.setIgnoredProxyHosts(values.get(Property.PROXY_IGNORED_HOSTS));
        proxyBuilder.setUsername(values.get(Property.PROXY_USERNAME));
        proxyBuilder.setPassword(values.get(Property.PROXY_PASSWORD));
        proxyBuilder.setPasswordLength(proxyPasswordLength());
        proxyBuilder.setNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
        proxyBuilder.setNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
        return proxyBuilder.buildObject();
    }

    @Override
    public AbstractValidator createValidator() {
        validator.setHubUrl(url());
        validator.setUsername(values.get(Property.USERNAME));
        validator.setPassword(values.get(Property.PASSWORD));
        validator.setPasswordLength(passwordLength());
        validator.setApiToken(apiToken());
        validator.setTimeout(values.get(Property.TIMEOUT));
        validator.setProxyHost(values.get(Property.PROXY_HOST));
        validator.setProxyPort(values.get(Property.PROXY_PORT));
        validator.setIgnoredProxyHosts(values.get(Property.PROXY_IGNORED_HOSTS));
        validator.setProxyUsername(values.get(Property.PROXY_USERNAME));
        validator.setProxyPassword(values.get(Property.PROXY_PASSWORD));
        validator.setProxyPasswordLength(proxyPasswordLength());
        validator.setAlwaysTrustServerCertificate(trustCert());
        validator.setProxyNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
        validator.setProxyNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
        return validator;
    }

    public void setFromProperties(final Map<String, String> properties) {
        for (final Property configProperty : Property.values()) {
            if (configProperty.isWithin(properties.keySet())) {
                final String value = configProperty.getValueFrom(properties);
                final String setterMethodName = configProperty.getBuilderPropertySetterName();
                try {
                    final Method setter = this.getClass().getMethod(setterMethodName, String.class);
                    setter.invoke(this, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    // who cares - ekerwin 2018-05-15
                }
            }
        }
    }

    public void setFromProperties(final Properties properties) {
        final Map<String, String> propertiesMap = new HashMap<>();
        for (final String propertyName : properties.stringPropertyNames()) {
            propertiesMap.put(propertyName, properties.getProperty(propertyName));
        }

        setFromProperties(propertiesMap);
    }

    private String url() {
        return values.get(Property.URL);
    }

    private String apiToken() {
        return values.get(Property.API_TOKEN);
    }

    private int timeoutSeconds() {
        return NumberUtils.toInt(values.get(Property.TIMEOUT), DEFAULT_TIMEOUT_SECONDS);
    }

    private int passwordLength() {
        return NumberUtils.toInt(values.get(Property.PASSWORD_LENGTH), 0);
    }

    private int proxyPasswordLength() {
        return NumberUtils.toInt(values.get(Property.PROXY_PASSWORD_LENGTH), 0);
    }

    private boolean trustCert() {
        return Boolean.parseBoolean(values.get(Property.TRUST_CERT));
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

    // setters for the values of HubServerConfigBuilder
    @Deprecated
    /**
     * @deprecated Please use setUrl(final String url) instead.
     */
    public void setHubUrl(final String hubUrl) {
        setUrl(hubUrl);
    }

    @Deprecated
    /**
     * @deprecated Please use setTrustCert(final boolean trustCert) instead.
     */
    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        setTrustCert(alwaysTrustServerCertificate);
    }

    public void setUrl(final String url) {
        values.put(Property.URL, url);
    }

    public void setUsername(final String username) {
        values.put(Property.USERNAME, username);
    }

    public void setPassword(final String password) {
        values.put(Property.PASSWORD, password);
    }

    /**
     * IMPORTANT : The password length should only be set if the password is already encrypted
     */
    public void setPasswordLength(final String passwordLength) {
        values.put(Property.PASSWORD_LENGTH, passwordLength);
    }

    /**
     * IMPORTANT : The password length should only be set if the password is already encrypted
     */
    public void setPasswordLength(final int passwordLength) {
        setPasswordLength(String.valueOf(passwordLength));
    }

    public void setApiToken(final String apiToken) {
        values.put(Property.API_TOKEN, apiToken);
    }

    public void setTimeout(final String timeout) {
        values.put(Property.TIMEOUT, timeout);
    }

    public void setTimeout(final int timeout) {
        setTimeout(String.valueOf(timeout));
    }

    public void setProxyHost(final String proxyHost) {
        values.put(Property.PROXY_HOST, proxyHost);
    }

    public void setProxyPort(final String proxyPort) {
        values.put(Property.PROXY_PORT, proxyPort);
    }

    public void setProxyPort(final int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
    }

    public void setProxyIgnoredHosts(final String proxyIgnoredHosts) {
        values.put(Property.PROXY_IGNORED_HOSTS, proxyIgnoredHosts);
    }

    public void setProxyUsername(final String proxyUsername) {
        values.put(Property.PROXY_USERNAME, proxyUsername);
    }

    public void setProxyPassword(final String proxyPassword) {
        values.put(Property.PROXY_PASSWORD, proxyPassword);
    }

    /**
     * IMPORTANT : The proxy password length should only be set if the proxy password is already encrypted
     */
    public void setProxyPasswordLength(final String proxyPasswordLength) {
        values.put(Property.PROXY_PASSWORD_LENGTH, proxyPasswordLength);
    }

    /**
     * IMPORTANT : The proxy password length should only be set if the proxy password is already encrypted
     */
    public void setProxyPasswordLength(final int proxyPasswordLength) {
        setProxyPasswordLength(String.valueOf(proxyPasswordLength));
    }

    public void setProxyNtlmDomain(final String proxyNtlmDomain) {
        values.put(Property.PROXY_NTLM_DOMAIN, proxyNtlmDomain);
    }

    public void setProxyNtlmWorkstation(final String proxyNtlmWorkstation) {
        values.put(Property.PROXY_NTLM_WORKSTATION, proxyNtlmWorkstation);
    }

    public void setTrustCert(final String trustCert) {
        values.put(Property.TRUST_CERT, trustCert);
    }

    public void setTrustCert(final boolean trustCert) {
        setTrustCert(String.valueOf(trustCert));
    }

    public enum Property {
        URL,
        USERNAME,
        PASSWORD,
        PASSWORD_LENGTH,
        API_TOKEN,
        TIMEOUT,
        PROXY_HOST,
        PROXY_PORT,
        PROXY_IGNORED_HOSTS,
        PROXY_USERNAME,
        PROXY_PASSWORD,
        PROXY_PASSWORD_LENGTH,
        PROXY_NTLM_DOMAIN,
        PROXY_NTLM_WORKSTATION,
        TRUST_CERT;

        private final String environmentVariableKey;
        private final String propertyKey;
        private final String builderPropertyName;
        private final String builderPropertySetterName;

        private Property() {
            final String name = name();
            environmentVariableKey = HUB_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX + name;
            propertyKey = environmentVariableKey.toLowerCase().replace("_", ".");

            final String camelCaseName = WordUtils.capitalizeFully(name, '_').replace("_", "");
            builderPropertyName = StringUtils.uncapitalize(camelCaseName);
            builderPropertySetterName = "set" + camelCaseName;
        }

        public boolean isWithin(final Set<String> keys) {
            return keys.contains(environmentVariableKey) || keys.contains(propertyKey);
        }

        public String getValueFrom(final Map<String, String> values) {
            if (values.containsKey(propertyKey)) {
                return values.get(propertyKey);
            } else {
                return values.get(environmentVariableKey);
            }
        }

        public String getEnvironmentVariableKey() {
            return environmentVariableKey;
        }

        public String getPropertyKey() {
            return propertyKey;
        }

        public String getBuilderPropertyName() {
            return builderPropertyName;
        }

        public String getBuilderPropertySetterName() {
            return builderPropertySetterName;
        }

    }

}
