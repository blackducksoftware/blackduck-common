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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

public class CredentialsRestConnection extends BlackDuckRestConnection {
    private final Credentials credentials;

    public CredentialsRestConnection(final IntLogger logger, final URL baseUrl, final Credentials credentials, final int timeout, final ProxyInfo proxyInfo) {
        super(logger, baseUrl, timeout, proxyInfo);
        this.credentials = credentials;
    }

    @Override
    public void populateHttpClientBuilder(final HttpClientBuilder httpClientBuilder, final RequestConfig.Builder defaultRequestConfigBuilder) throws IntegrationException {
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
    }

    /**
     * Gets the cookie for the Authorized connection to the Hub server. Returns the response code from the connection.
     */
    @Override
    public void authenticateWithBlackDuck() throws IntegrationException {
        final URL securityUrl;
        try {
            securityUrl = new URL(getBaseUrl(), "j_spring_security_check");
        } catch (final MalformedURLException e) {
            throw new IntegrationException("Error constructing the login URL: " + e.getMessage(), e);
        }

        final List<NameValuePair> bodyValues = new ArrayList<>();
        bodyValues.add(new BasicNameValuePair("j_username", credentials.getUsername()));
        bodyValues.add(new BasicNameValuePair("j_password", credentials.getPassword()));
        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, Charsets.UTF_8);

        final RequestBuilder requestBuilder = createRequestBuilder(HttpMethod.POST, null);
        requestBuilder.setCharset(Charsets.UTF_8);
        requestBuilder.setUri(securityUrl.toString());
        requestBuilder.setEntity(entity);
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
                // get the CSRF token
                final Header csrfToken = closeableHttpResponse.getFirstHeader(RestConstants.X_CSRF_TOKEN);
                if (csrfToken != null) {
                    addCommonRequestHeader(RestConstants.X_CSRF_TOKEN, csrfToken.getValue());
                } else {
                    logger.error("No CSRF token found when authenticating");
                }
            }
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
