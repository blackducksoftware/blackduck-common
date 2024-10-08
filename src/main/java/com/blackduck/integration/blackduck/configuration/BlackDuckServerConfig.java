/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.configuration;

import com.blackduck.integration.blackduck.http.client.ApiTokenBlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.CookieHeaderParser;
import com.blackduck.integration.blackduck.http.client.CredentialsBlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.client.cache.CachingHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.builder.Buildable;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.SilentIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.credentials.Credentials;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.ErrorResponse;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.rest.support.AuthenticationSupport;
import com.blackduck.integration.util.IntEnvironmentVariables;
import com.blackduck.integration.util.NameVersion;
import com.blackduck.integration.util.Stringable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigKeys.KEYS;

public class BlackDuckServerConfig extends Stringable implements Buildable {
    @Deprecated
    /**
     * deprecated Please use one of the other static methods for your situation: newApiBuilder(), newUserPassBuilder()
     */
    public static BlackDuckServerConfigBuilder newBuilder() {
        return new BlackDuckServerConfigBuilder(KEYS.all);
    }

    public static BlackDuckServerConfigBuilder newApiTokenBuilder() {
        return new BlackDuckServerConfigBuilder(KEYS.apiToken);
    }

    public static BlackDuckServerConfigBuilder newCredentialsBuilder() {
        return new BlackDuckServerConfigBuilder(KEYS.credentials);
    }

    private final HttpUrl blackDuckUrl;
    private final NameVersion solutionDetails;
    private final int timeoutSeconds;
    private final Credentials credentials;
    private final String apiToken;
    private final ProxyInfo proxyInfo;
    private final boolean alwaysTrustServerCertificate;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final AuthenticationSupport authenticationSupport;
    private final CookieHeaderParser cookieHeaderParser;
    private final ExecutorService executorService;

    BlackDuckServerConfig(
        HttpUrl url, NameVersion solutionDetails, int timeoutSeconds, Credentials credentials, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
        AuthenticationSupport authenticationSupport, CookieHeaderParser cookieHeaderParser, ExecutorService executorService) {
        this(url, solutionDetails, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, cookieHeaderParser, executorService, credentials,
            null);
    }

    BlackDuckServerConfig(
        HttpUrl url, NameVersion solutionDetails, int timeoutSeconds, String apiToken, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson, ObjectMapper objectMapper,
        AuthenticationSupport authenticationSupport, ExecutorService executorService) {
        this(url, solutionDetails, timeoutSeconds, proxyInfo, alwaysTrustServerCertificate, intEnvironmentVariables, gson, objectMapper, authenticationSupport, null, executorService, null, apiToken);
    }

    private BlackDuckServerConfig(HttpUrl url, NameVersion solutionDetails, int timeoutSeconds, ProxyInfo proxyInfo, boolean alwaysTrustServerCertificate, IntEnvironmentVariables intEnvironmentVariables, Gson gson,
        ObjectMapper objectMapper, AuthenticationSupport authenticationSupport, CookieHeaderParser cookieHeaderParser, ExecutorService executorService, Credentials credentials,
        String apiToken) {
        blackDuckUrl = url;
        this.solutionDetails = solutionDetails;
        this.credentials = credentials;
        this.apiToken = apiToken;
        this.timeoutSeconds = timeoutSeconds;
        this.proxyInfo = proxyInfo;
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.authenticationSupport = authenticationSupport;
        this.cookieHeaderParser = cookieHeaderParser;
        this.executorService = executorService;
    }

    public boolean shouldUseProxyForBlackDuck() {
        return proxyInfo != null && proxyInfo.shouldUseProxy();
    }

    public void print(IntLogger logger) {
        if (getBlackDuckUrl() != null) {
            logger.alwaysLog("--> Black Duck Server Url: " + getBlackDuckUrl());
        }
        if (getSolutionDetails() != null) {
            logger.alwaysLog("--> Solution Name: " + getSolutionDetails().getName());
            logger.alwaysLog("--> Solution Version: " + getSolutionDetails().getVersion());
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
        BlackDuckConnectionResult connectionResult = attemptConnection(logger);
        return connectionResult.isSuccess();
    }

    public BlackDuckConnectionResult attemptConnection(IntLogger logger) {
        String errorMessage = null;
        Exception exception = null;
        int httpStatusCode = 0;

        BlackDuckHttpClient blackDuckHttpClient = null;
        try {
            blackDuckHttpClient = createBlackDuckHttpClient(logger);
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
            exception = e;
        }

        if (null != errorMessage || null == blackDuckHttpClient) {
            logger.error(errorMessage);
            return BlackDuckConnectionResult.BLACK_DUCK_FAILURE(httpStatusCode, errorMessage, exception);
        }

        logger.info("A successful connection was made.");
        return BlackDuckConnectionResult.BLACK_DUCK_SUCCESS(httpStatusCode, blackDuckHttpClient);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(IntLogger logger) {
        BlackDuckHttpClient blackDuckHttpClient = createBlackDuckHttpClient(logger);
        return createBlackDuckServicesFactory(blackDuckHttpClient, logger);
    }

    public BlackDuckServicesFactory createCachedBlackDuckServicesFactory(IntLogger logger) {
        BlackDuckHttpClient blackDuckHttpClient = createCacheHttpClient(logger);
        return createBlackDuckServicesFactory(blackDuckHttpClient, logger);
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(BlackDuckHttpClient blackDuckHttpClient, IntLogger logger) {
        return new BlackDuckServicesFactory(intEnvironmentVariables, executorService, logger, blackDuckHttpClient, gson, objectMapper);
    }

    public BlackDuckHttpClient createBlackDuckHttpClient(IntLogger logger) {
        if (usingApiToken()) {
            return createApiTokenBlackDuckHttpClient(logger);
        } else {
            return createCredentialsBlackDuckHttpClient(logger);
        }
    }

    public CachingHttpClient createCacheHttpClient(IntLogger logger) {
        BlackDuckHttpClient blackDuckHttpClient = createBlackDuckHttpClient(logger);
        return new CachingHttpClient(blackDuckHttpClient);
    }

    public CredentialsBlackDuckHttpClient createCredentialsBlackDuckHttpClient(IntLogger logger) {
        return new CredentialsBlackDuckHttpClient(logger, gson, getTimeout(), isAlwaysTrustServerCertificate(), getProxyInfo(), getBlackDuckUrl(), getSolutionDetails(), authenticationSupport, getCredentials().orElse(null),
            cookieHeaderParser);
    }

    public ApiTokenBlackDuckHttpClient createApiTokenBlackDuckHttpClient(IntLogger logger) {
        return new ApiTokenBlackDuckHttpClient(logger, gson, getTimeout(), isAlwaysTrustServerCertificate(), getProxyInfo(), getBlackDuckUrl(), getSolutionDetails(), authenticationSupport, getApiToken().orElse(null));
    }

    public boolean usingApiToken() {
        return StringUtils.isNotBlank(apiToken);
    }

    public HttpUrl getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public NameVersion getSolutionDetails() {
        return solutionDetails;
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
