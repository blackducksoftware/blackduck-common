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

public class CredentialsBlackDuckHttpClient extends DefaultBlackDuckHttpClient {
    private final Credentials credentials;
    private final CookieHeaderParser cookieHeaderParser;

    @Deprecated
    /**
     * @deprecated Please provide a CookieHeaderParser.
     */
    public CredentialsBlackDuckHttpClient(
        IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, Credentials credentials,
        BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl, solutionDetails, authenticationSupport, blackDuckMediaTypeDiscovery);
        this.credentials = credentials;
        this.cookieHeaderParser = new CookieHeaderParser();

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null.");
        }
    }

    public CredentialsBlackDuckHttpClient(
        IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl baseUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, Credentials credentials,
        BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery, CookieHeaderParser cookieHeaderParser) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo, baseUrl, solutionDetails, authenticationSupport, blackDuckMediaTypeDiscovery);
        this.credentials = credentials;
        this.cookieHeaderParser = cookieHeaderParser;

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
            Optional<String> token = cookieHeaderParser.parseBearerToken(actualResponse.getAllHeaders());
            authenticationSupport.addBearerToken(logger, request, this, token);
        }
    }

}
