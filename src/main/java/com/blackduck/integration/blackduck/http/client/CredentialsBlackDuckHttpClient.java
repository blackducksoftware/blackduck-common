/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.credentials.Credentials;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.rest.support.AuthenticationSupport;
import com.blackduck.integration.util.NameVersion;
import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CredentialsBlackDuckHttpClient extends DefaultBlackDuckHttpClient {
    private final Credentials credentials;
    private final CookieHeaderParser cookieHeaderParser;

    public CredentialsBlackDuckHttpClient(
        IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, Credentials credentials,
        CookieHeaderParser cookieHeaderParser) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, solutionDetails, authenticationSupport);
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
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, StandardCharsets.UTF_8);

        return authenticationSupport.attemptAuthentication(this, getBlackDuckUrl(), "j_spring_security_check", entity);
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
