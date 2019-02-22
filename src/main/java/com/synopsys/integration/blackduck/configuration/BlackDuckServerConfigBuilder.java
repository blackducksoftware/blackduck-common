/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.exception.IntegrationCertificateException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.IntegrationBuilder;

public class BlackDuckServerConfigBuilder extends IntegrationBuilder<BlackDuckServerConfig> {
    public static final String BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX = "BLACKDUCK_";
    public static final String BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX = "blackduck.";

    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private final Map<Property, String> values = new HashMap<>();

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
    private Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
    private AuthenticationSupport authenticationSupport = new AuthenticationSupport();
    private ExecutorService executorService = null;

    public BlackDuckServerConfigBuilder() {
        EnumSet.allOf(Property.class).forEach(property -> {
            values.put(property, null);
        });
        values.put(Property.TIMEOUT, String.valueOf(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS));
    }

    @Override
    public BlackDuckServerConfig build() {
        try {
            return super.build();
        } catch (Exception e) {
            if (!e.getMessage().contains("SunCertPathBuilderException")) {
                throw e;
            }
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", getUrl()), e);
        }
    }

    @Override
    public BlackDuckServerConfig buildWithoutValidation() {
        URL blackDuckURL = null;
        try {
            String tempUrl = getUrl();
            if (!tempUrl.endsWith("/")) {
                blackDuckURL = new URL(tempUrl);
            } else {
                tempUrl = tempUrl.substring(0, tempUrl.length() - 1);
                blackDuckURL = new URL(tempUrl);
            }
        } catch (MalformedURLException e) {
        }

        ProxyInfo proxyInfo = getProxyInfo();
        if (StringUtils.isNotBlank(getApiToken())) {
            return new BlackDuckServerConfig(blackDuckURL, getTimemoutInSeconds(), getApiToken(), proxyInfo, isTrustCert(), intEnvironmentVariables, gson, objectMapper, authenticationSupport);
        } else {
            String username = get(Property.USERNAME);
            String password = get(Property.PASSWORD);
            CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
            credentialsBuilder.setUsernameAndPassword(username, password);
            Credentials credentials = credentialsBuilder.build();

            return new BlackDuckServerConfig(blackDuckURL, getTimemoutInSeconds(), credentials, proxyInfo, isTrustCert(), intEnvironmentVariables, gson, objectMapper, authenticationSupport);
        }
    }

    private ProxyInfo getProxyInfo() {
        String proxyHost = getProxyHost();

        if (StringUtils.isBlank(proxyHost)) {
            return ProxyInfo.NO_PROXY_INFO;
        }

        int proxyPort = getProxyPort();
        String username = get(Property.PROXY_USERNAME);
        String password = get(Property.PROXY_PASSWORD);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(username, password);
        Credentials proxyCredentials = credentialsBuilder.build();
        String proxyNtlmDomain = values.get(Property.PROXY_NTLM_DOMAIN);
        String proxyNtlmWorkstation = values.get(Property.PROXY_NTLM_WORKSTATION);

        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost(proxyHost);
        proxyInfoBuilder.setPort(proxyPort);
        proxyInfoBuilder.setCredentials(proxyCredentials);
        proxyInfoBuilder.setNtlmDomain(proxyNtlmDomain);
        proxyInfoBuilder.setNtlmWorkstation(proxyNtlmWorkstation);

        return proxyInfoBuilder.build();
    }

    public BlackDuckServerConfigBuilder setFromProperties(Map<String, String> properties) {
        for (Property configProperty : Property.values()) {
            if (configProperty.isWithin(properties.keySet())) {
                String value = configProperty.getValueFrom(properties);
                put(configProperty, value);
            }
        }
        return this;
    }

    public BlackDuckServerConfigBuilder setFromProperties(Properties properties) {
        Map<String, String> propertiesMap = new HashMap<>();
        for (String propertyName : properties.stringPropertyNames()) {
            propertiesMap.put(propertyName, properties.getProperty(propertyName));
        }

        return setFromProperties(propertiesMap);
    }

    @Override
    protected void validate(BuilderStatus builderStatus) {
        if (StringUtils.isBlank(getUrl())) {
            builderStatus.addErrorMessage("The Black Duck url must be specified.");
        } else {
            try {
                URL blackDuckURL = new URL(getUrl());
                blackDuckURL.toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                builderStatus.addErrorMessage(String.format("The provided Black Duck url (%s) is not a valid URL.", getUrl()));
            }
        }

        if (StringUtils.isBlank(getApiToken())) {
            CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
            credentialsBuilder.setUsername(values.get(Property.USERNAME));
            credentialsBuilder.setPassword(values.get(Property.PASSWORD));
            BuilderStatus credentialsBuilderStatus = credentialsBuilder.validateAndGetBuilderStatus();
            if (!credentialsBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(credentialsBuilderStatus.getErrorMessages());
            } else {
                Credentials credentials = credentialsBuilder.build();
                if (credentials.isBlank()) {
                    builderStatus.addErrorMessage("Either an API token or a username/password must be specified.");
                }
            }
        }
        CredentialsBuilder proxyCredentialsBuilder = new CredentialsBuilder();
        proxyCredentialsBuilder.setUsername(values.get(Property.PROXY_USERNAME));
        proxyCredentialsBuilder.setPassword(values.get(Property.PROXY_PASSWORD));
        BuilderStatus proxyCredentialsBuilderStatus = proxyCredentialsBuilder.validateAndGetBuilderStatus();
        if (!proxyCredentialsBuilderStatus.isValid()) {
            builderStatus.addErrorMessage("The proxy credentials were not valid.");
            builderStatus.addAllErrorMessages(proxyCredentialsBuilderStatus.getErrorMessages());
        } else {
            Credentials proxyCredentials = proxyCredentialsBuilder.build();
            ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
            proxyInfoBuilder.setCredentials(proxyCredentials);
            proxyInfoBuilder.setHost(values.get(Property.PROXY_HOST));
            proxyInfoBuilder.setPort(NumberUtils.toInt(values.get(Property.PROXY_PORT), 0));
            proxyInfoBuilder.setNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
            proxyInfoBuilder.setNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
            BuilderStatus proxyInfoBuilderStatus = proxyInfoBuilder.validateAndGetBuilderStatus();
            if (!proxyInfoBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(proxyInfoBuilderStatus.getErrorMessages());
            }
        }

        if (getTimemoutInSeconds() <= 0) {
            builderStatus.addErrorMessage("The timeout must be greater than zero.");
        }
    }

    public IntLogger getLogger() {
        return logger;
    }

    public BlackDuckServerConfigBuilder setLogger(IntLogger logger) {
        if (null != logger) {
            this.logger = logger;
        }
        return this;
    }

    public Optional<ExecutorService> getExecutorService() {
        return Optional.ofNullable(executorService);
    }

    public BlackDuckServerConfigBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public AuthenticationSupport getAuthenticationSupport() {
        return authenticationSupport;
    }

    public BlackDuckServerConfigBuilder setAuthenticationSupport(AuthenticationSupport authenticationSupport) {
        if (null != authenticationSupport) {
            this.authenticationSupport = authenticationSupport;
        }
        return this;
    }

    public Gson getGson() {
        return gson;
    }

    public BlackDuckServerConfigBuilder setGson(Gson gson) {
        if (null != gson) {
            this.gson = gson;
        }
        return this;
    }

    public IntEnvironmentVariables getIntEnvironmentVariables() {
        return intEnvironmentVariables;
    }

    public BlackDuckServerConfigBuilder setIntEnvironmentVariables(IntEnvironmentVariables intEnvironmentVariables) {
        if (null != intEnvironmentVariables) {
            this.intEnvironmentVariables = intEnvironmentVariables;
        }
        return this;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public BlackDuckServerConfigBuilder setObjectMapper(ObjectMapper objectMapper) {
        if (null != objectMapper) {
            this.objectMapper = objectMapper;
        }
        return this;
    }

    public String get(Property property) {
        return values.get(property);
    }

    public void put(Property property, String value) {
        values.put(property, value);
    }

    public String getUrl() {
        return get(Property.URL);
    }

    public BlackDuckServerConfigBuilder setUrl(String url) {
        put(Property.URL, url);
        return this;
    }

    public BlackDuckServerConfigBuilder setCredentials(Credentials credentials) {
        put(Property.USERNAME, credentials.getUsername().orElse(null));
        put(Property.PASSWORD, credentials.getPassword().orElse(null));
        return this;
    }

    public String getUsername() {
        return get(Property.USERNAME);
    }

    public BlackDuckServerConfigBuilder setUsername(String username) {
        put(Property.USERNAME, username);
        return this;
    }

    public String getPassword() {
        return get(Property.PASSWORD);
    }

    public BlackDuckServerConfigBuilder setPassword(String password) {
        put(Property.PASSWORD, password);
        return this;
    }

    public String getApiToken() {
        return get(Property.API_TOKEN);
    }

    public BlackDuckServerConfigBuilder setApiToken(String apiToken) {
        put(Property.API_TOKEN, apiToken);
        return this;
    }

    /**
     * @deprecated Please use getTimeoutInSeconds
     */
    @Deprecated
    public int getTimemout() {
        return getTimemoutInSeconds();
    }

    public int getTimemoutInSeconds() {
        return NumberUtils.toInt(get(Property.TIMEOUT), BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * @deprecated Please use setTimeoutInSeconds
     */
    @Deprecated
    public BlackDuckServerConfigBuilder setTimeout(String timeout) {
        put(Property.TIMEOUT, timeout);
        return this;
    }

    /**
     * @deprecated Please use setTimeoutInSeconds
     */
    @Deprecated
    public BlackDuckServerConfigBuilder setTimeout(int timeout) {
        setTimeout(String.valueOf(timeout));
        return this;
    }

    public BlackDuckServerConfigBuilder setTimeoutInSeconds(String timeout) {
        put(Property.TIMEOUT, timeout);
        return this;
    }

    public BlackDuckServerConfigBuilder setTimeoutInSeconds(int timeout) {
        setTimeoutInSeconds(String.valueOf(timeout));
        return this;
    }

    public String getProxyHost() {
        return get(Property.PROXY_HOST);
    }

    public BlackDuckServerConfigBuilder setProxyHost(String proxyHost) {
        put(Property.PROXY_HOST, proxyHost);
        return this;
    }

    public int getProxyPort() {
        return NumberUtils.toInt(get(Property.PROXY_PORT), 0);
    }

    public BlackDuckServerConfigBuilder setProxyPort(String proxyPort) {
        put(Property.PROXY_PORT, proxyPort);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyPort(int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
        return this;
    }

    public String getProxyUsername() {
        return get(Property.PROXY_USERNAME);
    }

    public BlackDuckServerConfigBuilder setProxyUsername(String proxyUsername) {
        put(Property.PROXY_USERNAME, proxyUsername);
        return this;
    }

    public String getProxyPassword() {
        return get(Property.PROXY_PASSWORD);
    }

    public BlackDuckServerConfigBuilder setProxyPassword(String proxyPassword) {
        put(Property.PROXY_PASSWORD, proxyPassword);
        return this;
    }

    public String getProxyNtlmDomain() {
        return get(Property.PROXY_NTLM_DOMAIN);
    }

    public BlackDuckServerConfigBuilder setProxyNtlmDomain(String proxyNtlmDomain) {
        put(Property.PROXY_NTLM_DOMAIN, proxyNtlmDomain);
        return this;
    }

    public String getProxyNtlmWorkstation() {
        return get(Property.PROXY_NTLM_WORKSTATION);
    }

    public BlackDuckServerConfigBuilder setProxyNtlmWorkstation(String proxyNtlmWorkstation) {
        put(Property.PROXY_NTLM_WORKSTATION, proxyNtlmWorkstation);
        return this;
    }

    public boolean isTrustCert() {
        return Boolean.parseBoolean(get(Property.TRUST_CERT));
    }

    public BlackDuckServerConfigBuilder setTrustCert(String trustCert) {
        put(Property.TRUST_CERT, trustCert);
        return this;
    }

    public BlackDuckServerConfigBuilder setTrustCert(boolean trustCert) {
        setTrustCert(String.valueOf(trustCert));
        return this;
    }

    public enum Property {
        URL,
        USERNAME,
        PASSWORD,
        API_TOKEN,
        TIMEOUT,
        PROXY_HOST,
        PROXY_PORT,
        PROXY_USERNAME,
        PROXY_PASSWORD,
        PROXY_NTLM_DOMAIN,
        PROXY_NTLM_WORKSTATION,
        TRUST_CERT;

        private final String blackDuckEnvironmentVariableKey;
        private final String blackDuckPropertyKey;

        private Property() {
            String name = name();
            blackDuckEnvironmentVariableKey = BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX + name;
            blackDuckPropertyKey = blackDuckEnvironmentVariableKey.toLowerCase().replace("_", ".");
        }

        public boolean isWithin(Set<String> keys) {
            return keys.contains(blackDuckEnvironmentVariableKey) || keys.contains(blackDuckPropertyKey);
        }

        public String getValueFrom(Map<String, String> values) {
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
    }

}
