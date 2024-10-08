/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client.cache;

import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.core.response.UrlResponse;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.ErrorResponse;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class CachingHttpClient implements BlackDuckHttpClient {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final Map<Request, Response> cache;

    public CachingHttpClient(BlackDuckHttpClient blackDuckHttpClient) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.cache = Collections.synchronizedMap(new LRUMap<>(1000));
    }

    public void emptyCache() {
        cache.clear();
    }

    @Override
    public <T extends BlackDuckResponse, U extends UrlResponse<T>> Response execute(BlackDuckRequest<T, U> blackDuckRequest) throws IntegrationException {
        Request request = blackDuckRequest.getRequest();
        if (HttpMethod.GET == request.getMethod() && cache.containsKey(request)) {
            return cache.get(request);
        }
        Response response = blackDuckHttpClient.execute(blackDuckRequest);

        // the usage of the response will determine whether or not it is cached, because we can only cache responses IFF they are retrieved by string content
        CacheableResponse cacheableResponse = new CacheableResponse(request, response, cache);
        return cacheableResponse;
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        return blackDuckHttpClient.attemptAuthentication();
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return blackDuckHttpClient.isAlreadyAuthenticated(request);
    }

    @Override
    public Optional<ErrorResponse> extractErrorResponse(String responseContent) {
        return blackDuckHttpClient.extractErrorResponse(responseContent);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        blackDuckHttpClient.handleErrorResponse(request, response);
    }

    @Override
    public void throwExceptionForError(Response response) throws IntegrationException {
        blackDuckHttpClient.throwExceptionForError(response);
    }

    @Override
    public HttpUrl getBlackDuckUrl() {
        return blackDuckHttpClient.getBlackDuckUrl();
    }

    @Override
    public String getUserAgentString() {
        return blackDuckHttpClient.getUserAgentString();
    }

    @Override
    public HttpClientBuilder getHttpClientBuilder() {
        return blackDuckHttpClient.getHttpClientBuilder();
    }

    @Override
    public int getTimeoutInSeconds() {
        return blackDuckHttpClient.getTimeoutInSeconds();
    }

    @Override
    public boolean isAlwaysTrustServerCertificate() {
        return blackDuckHttpClient.isAlwaysTrustServerCertificate();
    }

    @Override
    public ProxyInfo getProxyInfo() {
        return blackDuckHttpClient.getProxyInfo();
    }

    @Override
    public IntLogger getLogger() {
        return blackDuckHttpClient.getLogger();
    }

    @Override
    public Gson getGson() {
        return blackDuckHttpClient.getGson();
    }

}
