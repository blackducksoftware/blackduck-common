/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.api.core.BlackDuckResponse;
import com.blackduck.integration.blackduck.api.core.response.UrlResponse;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.ErrorResponse;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Optional;

public interface BlackDuckHttpClient {
    <T extends BlackDuckResponse, U extends UrlResponse<T>> Response execute(BlackDuckRequest<T, U> blackDuckRequest) throws IntegrationException;

    Response attemptAuthentication() throws IntegrationException;

    boolean isAlreadyAuthenticated(HttpUriRequest request);

    Optional<ErrorResponse> extractErrorResponse(String responseContent);

    void handleErrorResponse(HttpUriRequest request, Response response);

    void throwExceptionForError(Response response) throws IntegrationException;

    HttpUrl getBlackDuckUrl();

    String getUserAgentString();

    HttpClientBuilder getHttpClientBuilder();

    int getTimeoutInSeconds();

    boolean isAlwaysTrustServerCertificate();

    ProxyInfo getProxyInfo();

    IntLogger getLogger();

    Gson getGson();

}
