/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.builder.BuilderProperties;
import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.exception.IntegrationCertificateException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NoThreadExecutorService;

public class BlackDuckServerConfigBuilder extends IntegrationBuilder<BlackDuckServerConfig> {
    public static final BuilderPropertyKey URL_KEY = new BuilderPropertyKey("BLACKDUCK_URL");
    public static final BuilderPropertyKey USERNAME_KEY = new BuilderPropertyKey("BLACKDUCK_USERNAME");
    public static final BuilderPropertyKey PASSWORD_KEY = new BuilderPropertyKey("BLACKDUCK_PASSWORD");
    public static final BuilderPropertyKey API_TOKEN_KEY = new BuilderPropertyKey("BLACKDUCK_API_TOKEN");
    public static final BuilderPropertyKey TIMEOUT_KEY = new BuilderPropertyKey("BLACKDUCK_TIMEOUT");
    public static final BuilderPropertyKey PROXY_HOST_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_HOST");
    public static final BuilderPropertyKey PROXY_PORT_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_PORT");
    public static final BuilderPropertyKey PROXY_USERNAME_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_USERNAME");
    public static final BuilderPropertyKey PROXY_PASSWORD_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_PASSWORD");
    public static final BuilderPropertyKey PROXY_NTLM_DOMAIN_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_NTLM_DOMAIN");
    public static final BuilderPropertyKey PROXY_NTLM_WORKSTATION_KEY = new BuilderPropertyKey("BLACKDUCK_PROXY_NTLM_WORKSTATION");
    public static final BuilderPropertyKey TRUST_CERT_KEY = new BuilderPropertyKey("BLACKDUCK_TRUST_CERT");

    public static int DEFAULT_TIMEOUT_SECONDS = 120;

    private final BuilderProperties builderProperties;
    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.includeSystemEnv();
    private Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
    private AuthenticationSupport authenticationSupport = new AuthenticationSupport();
    private BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery = new BlackDuckMediaTypeDiscovery();
    private ExecutorService executorService = new NoThreadExecutorService();
    private RequestFactory requestFactory = BlackDuckServicesFactory.createDefaultRequestFactory();

    public BlackDuckServerConfigBuilder() {
        Set<BuilderPropertyKey> propertyKeys = new HashSet<>();
        propertyKeys.add(URL_KEY);
        propertyKeys.add(USERNAME_KEY);
        propertyKeys.add(PASSWORD_KEY);
        propertyKeys.add(API_TOKEN_KEY);
        propertyKeys.add(TIMEOUT_KEY);
        propertyKeys.add(PROXY_HOST_KEY);
        propertyKeys.add(PROXY_PORT_KEY);
        propertyKeys.add(PROXY_USERNAME_KEY);
        propertyKeys.add(PROXY_PASSWORD_KEY);
        propertyKeys.add(PROXY_NTLM_DOMAIN_KEY);
        propertyKeys.add(PROXY_NTLM_WORKSTATION_KEY);
        propertyKeys.add(TRUST_CERT_KEY);
        builderProperties = new BuilderProperties(propertyKeys);

        builderProperties.set(TIMEOUT_KEY, Integer.toString(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS));
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
        HttpUrl blackDuckUrl = null;
        try {
            blackDuckUrl = new HttpUrl(getUrl());
        } catch (IntegrationException e) {
        }

        ProxyInfo proxyInfo = getProxyInfo();
        if (StringUtils.isNotBlank(getApiToken())) {
            return new BlackDuckServerConfig(blackDuckUrl, getTimemoutInSeconds(), getApiToken(), proxyInfo, isTrustCert(), intEnvironmentVariables, gson, objectMapper, authenticationSupport, blackDuckMediaTypeDiscovery, executorService,
                requestFactory);
        } else {
            String username = getUsername();
            String password = getPassword();
            CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
            credentialsBuilder.setUsernameAndPassword(username, password);
            Credentials credentials = credentialsBuilder.build();

            return new BlackDuckServerConfig(blackDuckUrl, getTimemoutInSeconds(), credentials, proxyInfo, isTrustCert(), intEnvironmentVariables, gson, objectMapper, authenticationSupport, blackDuckMediaTypeDiscovery, executorService,
                requestFactory);
        }
    }

    @Override
    protected void validate(BuilderStatus builderStatus) {
        validateBlackDucUrl(builderStatus);

        if (StringUtils.isBlank(getApiToken())) {
            validateBlackDuckCredentials(builderStatus);
        }

        validateProxyDetails(builderStatus);

        if (getTimemoutInSeconds() <= 0) {
            builderStatus.addErrorMessage("The timeout must be greater than zero.");
        }
    }

    private void validateProxyDetails(BuilderStatus builderStatus) {
        CredentialsBuilder proxyCredentialsBuilder = new CredentialsBuilder();
        proxyCredentialsBuilder.setUsername(getProxyUsername());
        proxyCredentialsBuilder.setPassword(getProxyPassword());
        BuilderStatus proxyCredentialsBuilderStatus = proxyCredentialsBuilder.validateAndGetBuilderStatus();
        if (!proxyCredentialsBuilderStatus.isValid()) {
            builderStatus.addErrorMessage("The proxy credentials were not valid.");
            builderStatus.addAllErrorMessages(proxyCredentialsBuilderStatus.getErrorMessages());
        } else {
            Credentials proxyCredentials = proxyCredentialsBuilder.build();
            ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
            proxyInfoBuilder.setCredentials(proxyCredentials);
            proxyInfoBuilder.setHost(getProxyHost());
            proxyInfoBuilder.setPort(getProxyPort());
            proxyInfoBuilder.setNtlmDomain(getProxyNtlmDomain());
            proxyInfoBuilder.setNtlmWorkstation(getProxyNtlmWorkstation());
            BuilderStatus proxyInfoBuilderStatus = proxyInfoBuilder.validateAndGetBuilderStatus();
            if (!proxyInfoBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(proxyInfoBuilderStatus.getErrorMessages());
            }
        }
    }

    private void validateBlackDuckCredentials(BuilderStatus builderStatus) {
        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        credentialsBuilder.setUsername(getUsername());
        credentialsBuilder.setPassword(getPassword());
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

    private void validateBlackDucUrl(BuilderStatus builderStatus) {
        if (StringUtils.isBlank(getUrl())) {
            builderStatus.addErrorMessage("The Black Duck url must be specified.");
        } else {
            try {
                new HttpUrl(getUrl());
            } catch (IntegrationException e) {
                builderStatus.addErrorMessage(String.format("The provided Black Duck url (%s) is not a valid URL.", getUrl()));
            }
        }
    }

    public Set<BuilderPropertyKey> getKeys() {
        return builderProperties.getKeys();
    }

    public Set<String> getPropertyKeys() {
        return builderProperties.getPropertyKeys();
    }

    public Set<String> getEnvironmentVariableKeys() {
        return builderProperties.getEnvironmentVariableKeys();
    }

    public Map<BuilderPropertyKey, String> getProperties() {
        return builderProperties.getProperties();
    }

    public void setProperties(Set<? extends Map.Entry<String, String>> propertyEntries) {
        builderProperties.setProperties(propertyEntries);
    }

    public void setProperty(String key, String value) {
        builderProperties.setProperty(key, value);
    }

    public ProxyInfo getProxyInfo() {
        String proxyHost = getProxyHost();

        if (StringUtils.isBlank(proxyHost)) {
            return ProxyInfo.NO_PROXY_INFO;
        }

        int proxyPort = getProxyPort();
        String username = getProxyUsername();
        String password = getProxyPassword();
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(username, password);
        Credentials proxyCredentials = credentialsBuilder.build();
        String proxyNtlmDomain = getProxyNtlmDomain();
        String proxyNtlmWorkstation = getProxyNtlmWorkstation();

        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost(proxyHost);
        proxyInfoBuilder.setPort(proxyPort);
        proxyInfoBuilder.setCredentials(proxyCredentials);
        proxyInfoBuilder.setNtlmDomain(proxyNtlmDomain);
        proxyInfoBuilder.setNtlmWorkstation(proxyNtlmWorkstation);

        return proxyInfoBuilder.build();
    }

    public BlackDuckServerConfigBuilder setProxyInfo(ProxyInfo proxyInfo) {
        setProxyHost(proxyInfo.getHost().orElse(null));
        setProxyPort(proxyInfo.getPort());
        setProxyUsername(proxyInfo.getUsername().orElse(null));
        setProxyPassword(proxyInfo.getPassword().orElse(null));
        setProxyNtlmDomain(proxyInfo.getNtlmDomain().orElse(null));
        setProxyNtlmWorkstation(proxyInfo.getNtlmWorkstation().orElse(null));

        return this;
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

    public BlackDuckMediaTypeDiscovery getBlackDuckMediaTypeDiscovery() {
        return blackDuckMediaTypeDiscovery;
    }

    public BlackDuckServerConfigBuilder setBlackDuckMediaTypeDiscovery(BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        if (null != blackDuckMediaTypeDiscovery) {
            this.blackDuckMediaTypeDiscovery = blackDuckMediaTypeDiscovery;
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

    public String getUrl() {
        return builderProperties.get(URL_KEY);
    }

    public BlackDuckServerConfigBuilder setUrl(String url) {
        builderProperties.set(URL_KEY, url);
        return this;
    }

    public BlackDuckServerConfigBuilder setUrl(HttpUrl url) {
        builderProperties.set(URL_KEY, url.string());
        return this;
    }

    public BlackDuckServerConfigBuilder setCredentials(Credentials credentials) {
        builderProperties.set(USERNAME_KEY, credentials.getUsername().orElse(null));
        builderProperties.set(PASSWORD_KEY, credentials.getPassword().orElse(null));
        return this;
    }

    public String getUsername() {
        return builderProperties.get(USERNAME_KEY);
    }

    public BlackDuckServerConfigBuilder setUsername(String username) {
        builderProperties.set(USERNAME_KEY, username);
        return this;
    }

    public String getPassword() {
        return builderProperties.get(PASSWORD_KEY);
    }

    public BlackDuckServerConfigBuilder setPassword(String password) {
        builderProperties.set(PASSWORD_KEY, password);
        return this;
    }

    public String getApiToken() {
        return builderProperties.get(API_TOKEN_KEY);
    }

    public BlackDuckServerConfigBuilder setApiToken(String apiToken) {
        builderProperties.set(API_TOKEN_KEY, apiToken);
        return this;
    }

    public int getTimemoutInSeconds() {
        return NumberUtils.toInt(builderProperties.get(TIMEOUT_KEY), BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
    }

    public BlackDuckServerConfigBuilder setTimeoutInSeconds(String timeout) {
        builderProperties.set(TIMEOUT_KEY, timeout);
        return this;
    }

    public BlackDuckServerConfigBuilder setTimeoutInSeconds(int timeout) {
        setTimeoutInSeconds(String.valueOf(timeout));
        return this;
    }

    public String getProxyHost() {
        return builderProperties.get(PROXY_HOST_KEY);
    }

    public BlackDuckServerConfigBuilder setProxyHost(String proxyHost) {
        builderProperties.set(PROXY_HOST_KEY, proxyHost);
        return this;
    }

    public int getProxyPort() {
        return NumberUtils.toInt(builderProperties.get(PROXY_PORT_KEY), 0);
    }

    public BlackDuckServerConfigBuilder setProxyPort(String proxyPort) {
        builderProperties.set(PROXY_PORT_KEY, proxyPort);
        return this;
    }

    public BlackDuckServerConfigBuilder setProxyPort(int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
        return this;
    }

    public String getProxyUsername() {
        return builderProperties.get(PROXY_USERNAME_KEY);
    }

    public BlackDuckServerConfigBuilder setProxyUsername(String proxyUsername) {
        builderProperties.set(PROXY_USERNAME_KEY, proxyUsername);
        return this;
    }

    public String getProxyPassword() {
        return builderProperties.get(PROXY_PASSWORD_KEY);
    }

    public BlackDuckServerConfigBuilder setProxyPassword(String proxyPassword) {
        builderProperties.set(PROXY_PASSWORD_KEY, proxyPassword);
        return this;
    }

    public String getProxyNtlmDomain() {
        return builderProperties.get(PROXY_NTLM_DOMAIN_KEY);
    }

    public BlackDuckServerConfigBuilder setProxyNtlmDomain(String proxyNtlmDomain) {
        builderProperties.set(PROXY_NTLM_DOMAIN_KEY, proxyNtlmDomain);
        return this;
    }

    public String getProxyNtlmWorkstation() {
        return builderProperties.get(PROXY_NTLM_WORKSTATION_KEY);
    }

    public BlackDuckServerConfigBuilder setProxyNtlmWorkstation(String proxyNtlmWorkstation) {
        builderProperties.set(PROXY_NTLM_WORKSTATION_KEY, proxyNtlmWorkstation);
        return this;
    }

    public boolean isTrustCert() {
        return Boolean.parseBoolean(builderProperties.get(TRUST_CERT_KEY));
    }

    public BlackDuckServerConfigBuilder setTrustCert(String trustCert) {
        builderProperties.set(TRUST_CERT_KEY, trustCert);
        return this;
    }

    public BlackDuckServerConfigBuilder setTrustCert(boolean trustCert) {
        setTrustCert(String.valueOf(trustCert));
        return this;
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

}
