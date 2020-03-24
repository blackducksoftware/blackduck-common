/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class CredentialsBlackDuckHttpClient extends BlackDuckHttpClient {
    private final Credentials credentials;
    private final AuthenticationSupport authenticationSupport;

    public CredentialsBlackDuckHttpClient(
            IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, String baseUrl, AuthenticationSupport authenticationSupport, Credentials credentials) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl);
        this.credentials = credentials;
        this.authenticationSupport = authenticationSupport;

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null.");
        }
    }

    @Override
    public void populateHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        httpClientBuilder.setDefaultCookieStore(new BasicCookieStore());
        defaultRequestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleErrorResponse(this, request, response, RestConstants.X_CSRF_TOKEN);
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return request.containsHeader(RestConstants.X_CSRF_TOKEN);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        if (response.isStatusCodeSuccess()) {
            CloseableHttpResponse actualResponse = response.getActualResponse();
            Header csrfHeader = actualResponse.getFirstHeader(RestConstants.X_CSRF_TOKEN);
            String csrfHeaderValue = csrfHeader.getValue();
            if (null != csrfHeaderValue) {
                authenticationSupport.addAuthenticationHeader(this, request, RestConstants.X_CSRF_TOKEN, csrfHeaderValue);
            } else {
                logger.error("No CSRF token found when authenticating.");
            }
        }
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        List<NameValuePair> bodyValues = new ArrayList<>();
        bodyValues.add(new BasicNameValuePair("j_username", credentials.getUsername().orElse(null)));
        bodyValues.add(new BasicNameValuePair("j_password", credentials.getPassword().orElse(null)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, Charsets.UTF_8);

        return authenticationSupport.attemptAuthentication(this, getBaseUrl(), "j_spring_security_check", entity);
    }

}
