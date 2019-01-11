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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;

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
import com.synopsys.integration.util.IntegrationBuilder;

public class BlackDuckServerConfigBuilder extends IntegrationBuilder<BlackDuckServerConfig> {
    public static final String BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX = "BLACKDUCK_";
    public static final String BLACKDUCK_SERVER_CONFIG_PROPERTY_KEY_PREFIX = "blackduck.";

    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private final Map<Property, String> values = new HashMap<>();

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
    private AuthenticationSupport authenticationSupport = new AuthenticationSupport();

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
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", url()), e);
        }
    }

    @Override
    public BlackDuckServerConfig buildWithoutValidation() {
        URL blackDuckURL = null;
        try {
            String tempUrl = url();
            if (!tempUrl.endsWith("/")) {
                blackDuckURL = new URL(tempUrl);
            } else {
                tempUrl = tempUrl.substring(0, tempUrl.length() - 1);
                blackDuckURL = new URL(tempUrl);
            }
        } catch (MalformedURLException e) {
        }

        ProxyInfo proxyInfo = getProxyInfo();
        if (StringUtils.isNotBlank(apiToken())) {
            return new BlackDuckServerConfig(blackDuckURL, timeoutSeconds(), apiToken(), proxyInfo, trustCert(), gson, objectMapper, authenticationSupport);
        } else {
            String username = get(Property.USERNAME);
            String password = get(Property.PASSWORD);
            CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
            credentialsBuilder.setUsernameAndPassword(username, password);
            Credentials credentials = credentialsBuilder.build();

            return new BlackDuckServerConfig(blackDuckURL, timeoutSeconds(), credentials, proxyInfo, trustCert(), gson, objectMapper, authenticationSupport);
        }
    }

    private ProxyInfo getProxyInfo() {
        String proxyHost = values.get(Property.PROXY_HOST);

        if (StringUtils.isBlank(proxyHost)) {
            return ProxyInfo.NO_PROXY_INFO;
        }

        int proxyPort = NumberUtils.toInt(values.get(Property.PROXY_PORT), 0);
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
                String setterMethodName = configProperty.getBuilderPropertySetterName();
                try {
                    Method setter = getClass().getMethod(setterMethodName, String.class);
                    setter.invoke(this, value);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    // who cares - ekerwin 2018-05-15
                }
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
        if (StringUtils.isBlank(url())) {
            builderStatus.addErrorMessage("The Black Duck url must be specified.");
        } else {
            try {
                URL blackDuckURL = new URL(url());
                blackDuckURL.toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                builderStatus.addErrorMessage(String.format("The provided Black Duck url (%s) is not a valid URL.", url()));
            }
        }

        if (StringUtils.isBlank(apiToken())) {
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
        return NumberUtils.toInt(values.get(Property.TIMEOUT), BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
    }

    private boolean trustCert() {
        return Boolean.parseBoolean(values.get(Property.TRUST_CERT));
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

    // setters for the values of BlackDuckServerConfigBuilder
    public BlackDuckServerConfigBuilder setUrl(String url) {
        values.put(Property.URL, url);
        return this;
    }

    public BlackDuckServerConfigBuilder setCredentials(Credentials credentials) {
        values.put(Property.USERNAME, credentials.getUsername().orElse(null));
        values.put(Property.PASSWORD, credentials.getPassword().orElse(null));
        return this;
    }

    public BlackDuckServerConfigBuilder setUsername(String username) {
        values.put(Property.USERNAME, username);
        return this;
    }

    public BlackDuckServerConfigBuilder setPassword(String password) {
        values.put(Property.PASSWORD, password);
        return this;
    }

    public BlackDuckServerConfigBuilder setApiToken(String apiToken) {
        values.put(Property.API_TOKEN, apiToken);
        return this;
    }

    public BlackDuckServerConfigBuilder setTimeout(String timeout) {
        values.put(Property.TIMEOUT, timeout);
        return this;
    }

    public BlackDuckServerConfigBuilder setTimeout(int timeout) {
        setTimeout(String.valueOf(timeout));
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyHost(String proxyHost) {
        values.put(Property.PROXY_HOST, proxyHost);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyPort(String proxyPort) {
        values.put(Property.PROXY_PORT, proxyPort);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyPort(int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyUsername(String proxyUsername) {
        values.put(Property.PROXY_USERNAME, proxyUsername);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyPassword(String proxyPassword) {
        values.put(Property.PROXY_PASSWORD, proxyPassword);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyNtlmDomain(String proxyNtlmDomain) {
        values.put(Property.PROXY_NTLM_DOMAIN, proxyNtlmDomain);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyNtlmWorkstation(String proxyNtlmWorkstation) {
        values.put(Property.PROXY_NTLM_WORKSTATION, proxyNtlmWorkstation);
        return this;
    }

    public BlackDuckServerConfigBuilder setTrustCert(String trustCert) {
        values.put(Property.TRUST_CERT, trustCert);
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
        private final String builderPropertyName;
        private final String builderPropertySetterName;

        private Property() {
            String name = name();
            blackDuckEnvironmentVariableKey = BlackDuckServerConfigBuilder.BLACKDUCK_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX + name;
            blackDuckPropertyKey = blackDuckEnvironmentVariableKey.toLowerCase().replace("_", ".");

            String camelCaseName = WordUtils.capitalizeFully(name, '_').replace("_", "");
            builderPropertyName = StringUtils.uncapitalize(camelCaseName);
            builderPropertySetterName = "set" + camelCaseName;
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

        public String getBuilderPropertyName() {
            return builderPropertyName;
        }

        public String getBuilderPropertySetterName() {
            return builderPropertySetterName;
        }

    }

}
