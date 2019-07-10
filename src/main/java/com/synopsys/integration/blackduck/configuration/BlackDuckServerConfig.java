/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.synopsys.integration.rest.response.ErrorResponse;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.rest.ApiTokenBlackDuckHttpClient;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.rest.CredentialsBlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NoThreadExecutorService;
import com.synopsys.integration.util.Stringable;

public class BlackDuckServerConfig extends Stringable implements Buildable {
    public static BlackDuckServerConfigBuilder newBuilder() {
        return new BlackDuckServerConfigBuilder();
    }

    private final URL blackDuckUrl;
    private final int timeoutSeconds;
    private final Credentials credentials;
    private final String apiToken;
    private final ProxyInfo proxyInfo;
    private final boolean alwaysTrustServerCertificate;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final AuthenticationSupport authenticationSupport;
    private final ExecutorService executorService;

    BlackDuckServerConfig(
            URL url, int timeoutSeconds, Credentials credentials, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
            AuthenticationSupport authenticationSupport,
            ExecutorService executorService) {
        this(url, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, executorService, credentials, null);
    }

    /**
     * @deprecated Please provide an ExecutorService - for no change, you can provide an instance of NoThreadExecutorService
     */
    @Deprecated
    BlackDuckServerConfig(
            URL url, int timeoutSeconds, Credentials credentials, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
            AuthenticationSupport authenticationSupport) {
        this(url, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, new NoThreadExecutorService(), credentials, null);
    }

    BlackDuckServerConfig(
            URL url, int timeoutSeconds, String apiToken, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
            AuthenticationSupport authenticationSupport,
            ExecutorService executorService) {
        this(url, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, executorService, null, apiToken);
    }

    /**
     * @deprecated Please provide an ExecutorService - for no change, you can provide an instance of NoThreadExecutorService
     */
    @Deprecated
    BlackDuckServerConfig(
            URL url, int timeoutSeconds, String apiToken, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
            AuthenticationSupport authenticationSupport) {
        this(url, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, new NoThreadExecutorService(), null, apiToken);
    }

    private BlackDuckServerConfig(URL url, int timeoutSeconds, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
            AuthenticationSupport authenticationSupport, ExecutorService executorService,
            Credentials credentials,
            String apiToken) {
        this.credentials = credentials;
        this.apiToken = apiToken;
        blackDuckUrl = url;
        this.timeoutSeconds = timeoutSeconds;
        this.proxyInfo = proxyInfo;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.authenticationSupport = authenticationSupport;
        this.executorService = executorService;
    }

    public boolean shouldUseProxyForBlackDuck() {
        return proxyInfo != null && proxyInfo.shouldUseProxy();
    }

    public void print(IntLogger logger) {
        if (getBlackDuckUrl() != null) {
            logger.alwaysLog("--> Black Duck Server Url: " + getBlackDuckUrl());
        }
        if (getCredentials().isPresent() && getCredentials().get().getUsername().isPresent()) {
            logger.alwaysLog("--> Black Duck User: " + getCredentials().get().getUsername().get());
        }
        if (StringUtils.isNotBlank(apiToken)) {
            logger.alwaysLog("--> Black Duck API Token Used");
        }
        if (alwaysTrustServerCertificate) {
            logger.alwaysLog("--> Trust Black Duck certificate: " + isAlwaysTrustServerCertificate());
        }
        if (proxyInfo != null && proxyInfo.shouldUseProxy()) {
            if (StringUtils.isNotBlank(proxyInfo.getHost().orElse(null))) {
                logger.alwaysLog("--> Proxy Host: " + proxyInfo.getHost());
            }
            if (proxyInfo.getPort() > 0) {
                logger.alwaysLog("--> Proxy Port: " + proxyInfo.getPort());
            }
            if (StringUtils.isNotBlank(proxyInfo.getUsername().orElse(null))) {
                logger.alwaysLog("--> Proxy Username: " + proxyInfo.getUsername());
            }
        }
    }

    public boolean canConnect() {
        return canConnect(new SilentIntLogger());
    }

    public boolean canConnect(IntLogger logger) {
        ConnectionResult connectionResult = attemptConnection(logger);
        return connectionResult.isSuccess();
    }

    public ConnectionResult attemptConnection(IntLogger logger) {
        String errorMessage = null;
        int httpStatusCode = 0;

        try {
            BlackDuckHttpClient blackDuckHttpClient = createBlackDuckHttpClient(logger);
            try (Response response = blackDuckHttpClient.attemptAuthentication()) {
                // if you get an error response, you know that a connection could not be made
                httpStatusCode = response.getStatusCode();
                if (response.isStatusCodeError()) {
                    String httpResponseContent = response.getContentString();
                    Optional<ErrorResponse> errorResponse = blackDuckHttpClient.extractErrorResponse(httpResponseContent);
                    if (errorResponse.isPresent()) {
                        errorMessage = errorResponse.get().getErrorMessage();
                    } else {
                        errorMessage = "The connection was not successful for an unknown reason. If an api token is being used, it could be incorrect.";
                    }
                }
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        if (null != errorMessage) {
            logger.error(errorMessage);
            return ConnectionResult.FAILURE(httpStatusCode, errorMessage);
        }

        logger.info("A successful connection was made.");
        return ConnectionResult.SUCCESS(httpStatusCode);
    }

    /**
     * @deprecated The gson and objectMapper instances don't need to be passed in - they should be set in the builder.
     */
    @Deprecated
    public BlackDuckServicesFactory createBlackDuckServicesFactory(Gson gson, ObjectMapper objectMapper, IntLogger logger) {
        BlackDuckHttpClient blackDuckRestConnection = createBlackDuckHttpClient(logger);
        return new BlackDuckServicesFactory(intEnvironmentVariables, gson, objectMapper, blackDuckRestConnection, logger);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(IntLogger logger) {
        BlackDuckHttpClient blackDuckRestConnection = createBlackDuckHttpClient(logger);
        return new BlackDuckServicesFactory(intEnvironmentVariables, gson, objectMapper, executorService, blackDuckRestConnection, logger);
    }

    public BlackDuckHttpClient createBlackDuckHttpClient(IntLogger logger) {
        if (usingApiToken()) {
            return createApiTokenBlackDuckHttpClient(logger);
        } else {
            return createCredentialsBlackDuckHttpClient(logger);
        }
    }

    public CredentialsBlackDuckHttpClient createCredentialsBlackDuckHttpClient(IntLogger logger) {
        return new CredentialsBlackDuckHttpClient(logger, getTimeout(), isAlwaysTrustServerCertificate(), getProxyInfo(), getBlackDuckUrl().toString(), authenticationSupport, getCredentials().orElse(null));
    }

    public ApiTokenBlackDuckHttpClient createApiTokenBlackDuckHttpClient(IntLogger logger) {
        return new ApiTokenBlackDuckHttpClient(logger, getTimeout(), isAlwaysTrustServerCertificate(), getProxyInfo(), getBlackDuckUrl().toString(), gson, authenticationSupport, getApiToken().orElse(null));
    }

    public boolean usingApiToken() {
        return StringUtils.isNotBlank(apiToken);
    }

    public URL getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public Optional<Credentials> getCredentials() {
        return Optional.ofNullable(credentials);
    }

    public Optional<String> getApiToken() {
        return Optional.ofNullable(apiToken);
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public int getTimeout() {
        return timeoutSeconds;
    }

    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

}
