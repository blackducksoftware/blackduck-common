/**
 * hub-common
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

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

/**
 * Connection to the Hub application which authenticates using the API token feature (added in Hub 4.4.0)
 */
public class ApiTokenRestConnection extends BlackDuckRestConnection {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String apiToken;

    public ApiTokenRestConnection(final IntLogger logger, final URL hubBaseUrl, final String apiToken, final int timeout, final ProxyInfo proxyInfo) {
        super(logger, hubBaseUrl, timeout, proxyInfo);
        this.apiToken = apiToken;
    }

    @Override
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException {
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns the response code from the connection.
     */
    @Override
    public void authenticateWithBlackDuck() throws IntegrationException {
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
            try (final CloseableHttpResponse closeableHttpResponse = getClient().execute(request)) {
                logResponseHeaders(closeableHttpResponse);
                final Response response = new Response(closeableHttpResponse);
                final int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
                final String statusMessage = closeableHttpResponse.getStatusLine().getReasonPhrase();
                if (statusCode < RestConstants.OK_200 || statusCode >= RestConstants.MULT_CHOICE_300) {
                    final String httpResponseContent = response.getContentString();
                    throw new IntegrationRestException(statusCode, statusMessage, httpResponseContent, String.format("Connection Error: %s %s", statusCode, statusMessage));
                } else {
                    addCommonRequestHeader(AUTHORIZATION_HEADER, "Bearer " + readBearerToken(closeableHttpResponse));
                }
            } catch (final IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
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
