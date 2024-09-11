/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client;

import java.util.Optional;

import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlResponse;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;

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
