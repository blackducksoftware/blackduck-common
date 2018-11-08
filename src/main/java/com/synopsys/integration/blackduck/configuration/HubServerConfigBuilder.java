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
package com.synopsys.integration.blackduck.configuration;

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

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.exception.IntegrationCertificateException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public class HubServerConfigBuilder extends IntegrationBuilder<HubServerConfig> {
    public static final String BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX = "BLACKDUCK_";
    public static final String BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX = "blackduck.";

    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private final Map<Property, String> values = new HashMap<>();
    private IntLogger logger;

    public HubServerConfigBuilder() {
        EnumSet.allOf(Property.class).forEach(property -> {
            values.put(property, null);
        });
        values.put(Property.TIMEOUT, String.valueOf(DEFAULT_TIMEOUT_SECONDS));
    }

    @Override
    public HubServerConfig build() {
        try {
            return super.build();
        } catch (final Exception e) {
            if (!e.getMessage().contains("SunCertPathBuilderException")) {
                throw e;
            }
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", url()), e);
        }
    }

    @Override
    public HubServerConfig buildWithoutValidation() {
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

        final ProxyInfo proxyInfo = getProxyInfo();
        if (StringUtils.isNotBlank(apiToken())) {
            return new HubServerConfig(hubURL, timeoutSeconds(), apiToken(), proxyInfo, trustCert());
        } else {
            final Credentials credentials = new Credentials(values.get(Property.USERNAME), values.get(Property.PASSWORD));
            return new HubServerConfig(hubURL, timeoutSeconds(), credentials, proxyInfo, trustCert());
        }
    }

    private ProxyInfo getProxyInfo() {
        final String proxyHost = values.get(Property.PROXY_HOST);
        final int proxyPort = NumberUtils.toInt(values.get(Property.PROXY_PORT), 0);
        final String ignoredProxyHosts = values.get(Property.PROXY_IGNORED_HOSTS);
        final Credentials proxyCredentials = new Credentials(values.get(Property.PROXY_USERNAME), values.get(Property.PROXY_PASSWORD));
        final String proxyNtlmDomain = values.get(Property.PROXY_NTLM_DOMAIN);
        final String proxyNtlmWorkstation = values.get(Property.PROXY_NTLM_WORKSTATION);

        final ProxyInfo proxyInfo = new ProxyInfo(proxyHost, proxyPort, proxyCredentials, ignoredProxyHosts, proxyNtlmDomain, proxyNtlmWorkstation);
        return proxyInfo;
    }

    public void setFromProperties(final Map<String, String> properties) {
        for (final Property configProperty : Property.values()) {
            if (configProperty.isWithin(properties.keySet())) {
                final String value = configProperty.getValueFrom(properties);
                final String setterMethodName = configProperty.getBuilderPropertySetterName();
                try {
                    final Method setter = getClass().getMethod(setterMethodName, String.class);
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

    @Override
    protected void validate(final BuilderStatus builderStatus) {
        if (StringUtils.isBlank(values.get(Property.API_TOKEN))) {
            final CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
            credentialsBuilder.setUsername(values.get(Property.USERNAME));
            credentialsBuilder.setPassword(values.get(Property.PASSWORD));
            final BuilderStatus credentialsBuilderStatus = credentialsBuilder.validateAndGetBuilderStatus();
            if (!credentialsBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(credentialsBuilderStatus.getErrorMessages());
            } else {
                final Credentials credentials = credentialsBuilder.build();
                if (credentials.isBlank()) {
                    builderStatus.addErrorMessage("Either an API token or a username/password must be specified.");
                }
            }
        }
        final CredentialsBuilder proxyCredentialsBuilder = new CredentialsBuilder();
        proxyCredentialsBuilder.setUsername(values.get(Property.PROXY_USERNAME));
        proxyCredentialsBuilder.setPassword(values.get(Property.PROXY_PASSWORD));
        final BuilderStatus proxyCredentialsBuilderStatus = proxyCredentialsBuilder.validateAndGetBuilderStatus();
        if (!proxyCredentialsBuilderStatus.isValid()) {
            builderStatus.addErrorMessage("The proxy credentials were not valid.");
            builderStatus.addAllErrorMessages(proxyCredentialsBuilderStatus.getErrorMessages());
        } else {
            final Credentials proxyCredentials = proxyCredentialsBuilder.build();
            final ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
            proxyInfoBuilder.setCredentials(proxyCredentials);
            proxyInfoBuilder.setHost(values.get(Property.PROXY_HOST));
            proxyInfoBuilder.setPort(NumberUtils.toInt(values.get(Property.PROXY_PORT), 0));
            proxyInfoBuilder.setIgnoredProxyHosts(values.get(Property.PROXY_IGNORED_HOSTS));
            proxyInfoBuilder.setNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
            proxyInfoBuilder.setNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
            final BuilderStatus proxyInfoBuilderStatus = proxyInfoBuilder.validateAndGetBuilderStatus();
            if (!proxyInfoBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(proxyInfoBuilderStatus.getErrorMessages());
            }
        }

        if (timeoutSeconds() <= 0) {
            builderStatus.addErrorMessage("The timeout must be greater than zero.");
        }
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
    public void setUrl(final String url) {
        values.put(Property.URL, url);
    }

    public void setCredentials(final Credentials credentials) {
        values.put(Property.USERNAME, credentials.getUsername());
        values.put(Property.PASSWORD, credentials.getPassword());
    }

    public void setUsername(final String username) {
        values.put(Property.USERNAME, username);
    }

    public void setPassword(final String password) {
        values.put(Property.PASSWORD, password);
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
        API_TOKEN,
        TIMEOUT,
        PROXY_HOST,
        PROXY_PORT,
        PROXY_IGNORED_HOSTS,
        PROXY_USERNAME,
        PROXY_PASSWORD,
        PROXY_NTLM_DOMAIN,
        PROXY_NTLM_WORKSTATION,
        TRUST_CERT;

        private final String blackDuckEnvironmentVariableKey;
        private final String blackDuckPropertyKey;
        private final String builderPropertyName;
        private final String builderPropertySetterName;

        private Property() {
            final String name = name();
            blackDuckEnvironmentVariableKey = BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX + name;
            blackDuckPropertyKey = blackDuckEnvironmentVariableKey.toLowerCase().replace("_", ".");

            final String camelCaseName = WordUtils.capitalizeFully(name, '_').replace("_", "");
            builderPropertyName = StringUtils.uncapitalize(camelCaseName);
            builderPropertySetterName = "set" + camelCaseName;
        }

        public boolean isWithin(final Set<String> keys) {
            return keys.contains(blackDuckEnvironmentVariableKey) || keys.contains(blackDuckPropertyKey);
        }

        public String getValueFrom(final Map<String, String> values) {
            String key = blackDuckEnvironmentVariableKey;
            if (values.containsKey(blackDuckPropertyKey)) {
                key = blackDuckPropertyKey;
            }

            return values.get(key);
        }

        public String getBlackDuckEnvironmentVariableKey() {
            return blackDuckEnvironmentVariableKey;
        }

        public String getBlackDuckPropertyKey() {
            return blackDuckPropertyKey;
        }

        public String getBuilderPropertyName() {
            return builderPropertyName;
        }

        public String getBuilderPropertySetterName() {
            return builderPropertySetterName;
        }

    }

}
