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
package com.synopsys.integration.blackduck.rest;

import java.util.HashMap;
import java.util.Map;

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

        authenticationSupport.handleTokenErrorResponse(this, request, response);
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return authenticationSupport.isTokenAlreadyAuthenticated(request);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        authenticationSupport.completeTokenAuthenticationRequest(request, response, logger, gson, this, "bearerToken");
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        Map<String, String> headers = new HashMap<>();
        headers.put(AuthenticationSupport.AUTHORIZATION_HEADER, "token " + apiToken);
        // https://github.com/blackducksoftware/blackduck-common/issues/268
        headers.put("Content-Length", "0");

        return authenticationSupport.attemptAuthentication(this, getBaseUrl(), "api/tokens/authenticate", headers);
    }

}
