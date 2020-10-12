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
package com.synopsys.integration.blackduck.http.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.NameVersion;

public class CredentialsBlackDuckHttpClient extends BlackDuckHttpClient {
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String AUTHORIZATION_BEARER_PREFIX = "AUTHORIZATION_BEARER=";
    public static final String HEADER_VALUE_SEPARATOR = ";";

    private final Credentials credentials;

    public CredentialsBlackDuckHttpClient(
        IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, Credentials credentials,
        BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl, solutionDetails, authenticationSupport, blackDuckMediaTypeDiscovery);
        this.credentials = credentials;

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null.");
        }
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        List<NameValuePair> bodyValues = new ArrayList<>();
        bodyValues.add(new BasicNameValuePair("j_username", credentials.getUsername().orElse(null)));
        bodyValues.add(new BasicNameValuePair("j_password", credentials.getPassword().orElse(null)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, Charsets.UTF_8);

        return authenticationSupport.attemptAuthentication(this, getBaseUrl(), "j_spring_security_check", entity);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        if (response.isStatusCodeSuccess()) {
            CloseableHttpResponse actualResponse = response.getActualResponse();
            Optional<String> token = parseBearerToken(actualResponse);
            authenticationSupport.addBearerToken(logger, request, this, token);
        }
    }

    private Optional<String> parseBearerToken(CloseableHttpResponse response) {
        if (response.containsHeader(SET_COOKIE)) {
            String setCookieHeader = response.getFirstHeader(SET_COOKIE).getValue();
            return Optional.ofNullable(StringUtils.substringBetween(setCookieHeader, AUTHORIZATION_BEARER_PREFIX, HEADER_VALUE_SEPARATOR));
        }

        return Optional.empty();
    }

}
