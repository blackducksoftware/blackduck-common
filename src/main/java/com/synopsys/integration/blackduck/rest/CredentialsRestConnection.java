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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

public class CredentialsRestConnection extends BlackDuckRestConnection {
    private final Credentials credentials;

    public CredentialsRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo, final String baseUrl, final Credentials credentials) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl);
        this.credentials = credentials;

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null.");
        }
    }

    @Override
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) {
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
    }

    @Override
    public void handleErrorResponse(final HttpUriRequest request, final Response response) {
        super.handleErrorResponse(request, response);
        if (isUnauthorized(response.getStatusCode()) && request.containsHeader(RestConstants.X_CSRF_TOKEN)) {
            request.removeHeaders(RestConstants.X_CSRF_TOKEN);
            removeCommonRequestHeader(RestConstants.X_CSRF_TOKEN);
        }
    }

    @Override
    public void finalizeRequest(final HttpUriRequest request) {
        super.finalizeRequest(request);

        if (request.containsHeader(RestConstants.X_CSRF_TOKEN)) {
            // Already authenticated
            return;
        }

        final Optional<Header> csrfToken = requestCSRFTokenHeader();
        if (csrfToken.isPresent()) {
            final String headerValue = csrfToken.get().getValue();
            addCommonRequestHeader(RestConstants.X_CSRF_TOKEN, headerValue);
            request.addHeader(RestConstants.X_CSRF_TOKEN, headerValue);
        } else {
            getLogger().error("No CSRF token found when authenticating");
        }
    }

    private Optional<Header> requestCSRFTokenHeader() {
        try (final Response response = attemptAuthentication()) {
            if (response.isStatusCodeOkay()) {
                // Return the CSRF token
                return Optional.of(response.getActualResponse().getFirstHeader(RestConstants.X_CSRF_TOKEN));
            }
        } catch (IntegrationException | IOException e) {
            logger.error("Authentication was not successful", e);
        }
        return Optional.empty();
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException, IOException {
        final URL securityUrl;
        try {
            securityUrl = new URL(getBaseUrl(), "j_spring_security_check");
        } catch (final MalformedURLException e) {
            throw new IntegrationException("Error constructing the login URL: " + e.getMessage(), e);
        }

        final List<NameValuePair> bodyValues = new ArrayList<>();
        bodyValues.add(new BasicNameValuePair("j_username", credentials.getUsername().orElse(null)));
        bodyValues.add(new BasicNameValuePair("j_password", credentials.getPassword().orElse(null)));
        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, Charsets.UTF_8);

        final RequestBuilder requestBuilder = createRequestBuilder(HttpMethod.POST);
        requestBuilder.setCharset(Charsets.UTF_8);
        requestBuilder.setUri(securityUrl.toString());
        requestBuilder.setEntity(entity);
        final HttpUriRequest request = requestBuilder.build();
        logRequestHeaders(request);

        final CloseableHttpClient closeableHttpClient = getClientBuilder().build();
        final CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(request);
        logResponseHeaders(closeableHttpResponse);

        return new Response(request, closeableHttpClient, closeableHttpResponse);
    }

}
