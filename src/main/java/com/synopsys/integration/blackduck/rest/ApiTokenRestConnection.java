/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * Connection to the Black Duck application which authenticates using the API token feature
 */
public class ApiTokenRestConnection extends BlackDuckRestConnection {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String apiToken;

    public ApiTokenRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo, final String baseUrl, final String apiToken) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl);
        this.apiToken = apiToken;

        if (StringUtils.isBlank(apiToken)) {
            throw new IllegalArgumentException("No API token was found.");
        }
    }

    @Override
    public void handleErrorResponse(final HttpUriRequest request, final Response response) {
        super.handleErrorResponse(request, response);

        if (isUnauthorized(response.getStatusCode()) && request.containsHeader(AUTHORIZATION_HEADER)) {
            request.removeHeaders(AUTHORIZATION_HEADER);
            removeCommonRequestHeader(AUTHORIZATION_HEADER);
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Black Duck server. Returns the response code from the connection.
     */
    @Override
    public void finalizeRequest(final HttpUriRequest request) throws IntegrationException {
        super.finalizeRequest(request);

        if (request.containsHeader(AUTHORIZATION_HEADER)) {
            // Already authenticated
            return;
        }

        final Optional<String> bearerToken = retrieveBearerToken();
        if (bearerToken.isPresent()) {
            final String headerValue = "Bearer " + bearerToken.get();
            addCommonRequestHeader(AUTHORIZATION_HEADER, headerValue);
            request.addHeader(AUTHORIZATION_HEADER, headerValue);
        } else {
            getLogger().error("No Bearer token found when authenticating");
        }
    }

    private Optional<String> retrieveBearerToken() throws IntegrationException {
        final URL authenticationUrl;
        try {
            authenticationUrl = new URL(getBaseUrl(), "api/tokens/authenticate");
        } catch (final MalformedURLException e) {
            throw new IntegrationException("Error constructing the authentication URL: " + e.getMessage(), e);
        }

        if (StringUtils.isNotBlank(apiToken)) {
            final RequestBuilder requestBuilder = createRequestBuilder(HttpMethod.POST, getRequestHeaders());
            requestBuilder.setCharset(Charsets.UTF_8);
            requestBuilder.setUri(authenticationUrl.toString());
            final HttpUriRequest request = requestBuilder.build();
            logRequestHeaders(request);

            final CloseableHttpClient closeableHttpClient = getClientBuilder().build();
            final CloseableHttpResponse closeableHttpResponse;
            try {
                closeableHttpResponse = closeableHttpClient.execute(request);
                logResponseHeaders(closeableHttpResponse);
                try (final Response response = new Response(request, closeableHttpClient, closeableHttpResponse)) {
                    if (response.isStatusCodeOkay()) {
                        final String bearerToken = readBearerToken(closeableHttpResponse);
                        return Optional.of(bearerToken);
                    }
                } catch (final IOException e) {
                    throw new IntegrationException(e.getMessage(), e);
                }
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
        return Optional.empty();
    }

    private Map<String, String> getRequestHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION_HEADER, "token " + apiToken);

        return headers;
    }

    private String readBearerToken(final CloseableHttpResponse response) throws IOException {
        final JsonParser jsonParser = new JsonParser();
        String bodyToken = "";
        try (final InputStream inputStream = response.getEntity().getContent()) {
            bodyToken = IOUtils.toString(inputStream, Charsets.UTF_8);
        }
        final JsonObject bearerResponse = jsonParser.parse(bodyToken).getAsJsonObject();
        return bearerResponse.get("bearerToken").getAsString();
    }

}
