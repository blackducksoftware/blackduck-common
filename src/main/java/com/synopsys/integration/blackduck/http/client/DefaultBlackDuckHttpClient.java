/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.util.Optional;

import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.blackduck.useragent.BlackDuckCommon;
import com.synopsys.integration.blackduck.useragent.UserAgentBuilder;
import com.synopsys.integration.blackduck.useragent.UserAgentItem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.NameVersion;

public abstract class DefaultBlackDuckHttpClient extends AuthenticatingIntHttpClient implements BlackDuckHttpClient {
    private final Gson gson;
    private final HttpUrl blackDuckUrl;
    private final String userAgentString;

    protected final AuthenticationSupport authenticationSupport;

    public DefaultBlackDuckHttpClient(IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport) {
        this(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, new UserAgentItem(solutionDetails), BlackDuckCommon.createUserAgentItem(), authenticationSupport);
    }

    public DefaultBlackDuckHttpClient(IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, UserAgentItem solutionUserAgentItem,
        AuthenticationSupport authenticationSupport) {
        this(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, solutionUserAgentItem, BlackDuckCommon.createUserAgentItem(), authenticationSupport);
    }

    public DefaultBlackDuckHttpClient(IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, UserAgentItem solutionUserAgentItem,
        UserAgentItem blackDuckCommonUserAgentItem, AuthenticationSupport authenticationSupport) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo);

        if (null == blackDuckUrl) {
            throw new IllegalArgumentException("A Black Duck url is required, but was not provided.");
        }

        this.gson = gson;
        this.blackDuckUrl = blackDuckUrl;

        UserAgentBuilder userAgentBuilder = new UserAgentBuilder();
        userAgentBuilder.addUserAgent(solutionUserAgentItem);
        userAgentBuilder.addUserAgent(blackDuckCommonUserAgentItem);
        this.userAgentString = userAgentBuilder.createFullUserAgentString();

        this.authenticationSupport = authenticationSupport;
    }

    @Override
    public <T extends BlackDuckResponse, U extends UrlResponse<T>> Response execute(BlackDuckRequest<T, U> blackDuckRequest) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder(blackDuckRequest.getRequest());

        if (!requestBuilder.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
            requestBuilder.addHeader(HttpHeaders.USER_AGENT, userAgentString);
        }

        Request request = requestBuilder.build();

        try {
            return super.execute(request);
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return authenticationSupport.isTokenAlreadyAuthenticated(request);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleTokenErrorResponse(this, request, response);
    }

    @Override
    public void throwExceptionForError(Response response) throws IntegrationException {
        try {
            response.throwExceptionForError();
        } catch (IntegrationRestException e) {
            throw transformException(e);
        }
    }

    @Override
    public HttpUrl getBlackDuckUrl() {
        return blackDuckUrl;
    }

    @Override
    public String getUserAgentString() {
        return userAgentString;
    }

    @Override
    public HttpClientBuilder getHttpClientBuilder() {
        return getClientBuilder();
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.setRedirectStrategy(new BlackDuckRedirectStrategy());
    }

    private IntegrationException transformException(IntegrationRestException e) {
        String httpResponseContent = e.getHttpResponseContent();
        Optional<ErrorResponse> optionalErrorResponse = extractErrorResponse(httpResponseContent);
        // Not all IntegrationRestExceptions are from Black Duck - if we were able to
        // transform the IntegrationRestException, we want to return the resulting
        // BlackDuckApiException, otherwise, we want to ignore any potential
        // transformation and just return the original IntegrationRestException
        if (optionalErrorResponse.isPresent()) {
            ErrorResponse errorResponse = optionalErrorResponse.get();
            String apiExceptionErrorMessage = String.format("%s [HTTP Error]: %s", errorResponse.getErrorMessage(), e.getMessage());
            return new BlackDuckApiException(e, apiExceptionErrorMessage, errorResponse.getErrorCode());
        } else {
            return e;
        }
    }

}
