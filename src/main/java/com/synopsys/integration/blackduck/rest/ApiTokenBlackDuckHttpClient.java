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
package com.synopsys.integration.blackduck.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

/**
 * Connection to the Black Duck application which authenticates using the API token feature
 */
public class ApiTokenBlackDuckHttpClient extends BlackDuckHttpClient {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final Gson gson;
    private final AuthenticationSupport authenticationSupport;
    private final String apiToken;

    public ApiTokenBlackDuckHttpClient(IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, String baseUrl, Gson gson, AuthenticationSupport authenticationSupport, String apiToken) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl);
        this.gson = gson;
        this.authenticationSupport = authenticationSupport;
        this.apiToken = apiToken;

        if (StringUtils.isBlank(apiToken)) {
            throw new IllegalArgumentException("No API token was found.");
        }
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleErrorResponse(this, request, response, ApiTokenBlackDuckHttpClient.AUTHORIZATION_HEADER);
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(ApiTokenBlackDuckHttpClient.AUTHORIZATION_HEADER);
    }

    @Override
    public void authenticateRequest(HttpUriRequest request) throws IntegrationException {
        try (Response response = attemptAuthentication()) {
            if (response.isStatusCodeOkay()) {
                Optional<String> bearerToken = authenticationSupport.retrieveBearerToken(logger, gson, this, "bearerToken");
                authenticationSupport.resolveBearerToken(logger, this, request, ApiTokenBlackDuckHttpClient.AUTHORIZATION_HEADER, bearerToken);
            }
        } catch (IOException e) {
            throw new IntegrationException("The request could not be authenticated with the provided api token: " + e.getMessage(), e);
        }
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        Map<String, String> headers = new HashMap<>();
        headers.put(ApiTokenBlackDuckHttpClient.AUTHORIZATION_HEADER, "token " + apiToken);

        return authenticationSupport.attemptAuthentication(this, getBaseUrl(), "api/tokens/authenticate", headers);
    }

}
