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
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.response.Response;
import com.blackduck.integration.rest.support.AuthenticationSupport;
import com.blackduck.integration.util.NameVersion;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class BearerTokenBlackDuckHttpClient extends DefaultBlackDuckHttpClient {
    private final String bearerToken;

    public BearerTokenBlackDuckHttpClient(
        IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo,
        HttpUrl blackDuckUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, String bearerToken) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, solutionDetails, authenticationSupport);
        if (StringUtils.isBlank(bearerToken)) {
            throw new IllegalArgumentException("No bearer token was found.");
        }
        this.bearerToken = bearerToken;
        addCommonRequestHeader(AuthenticationSupport.AUTHORIZATION_HEADER, "Bearer " + bearerToken);
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        // GET /api/current-user acts as the connectivity probe for bearer token auth.
        // POST /api/tokens/authenticate cannot be used here as it requires an API key, not a bearer token.
        // The Authorization header is added explicitly because common headers may not propagate
        // through the Apache client bypass path used by authenticationSupport.attemptAuthentication.
        HttpUrl currentUserUrl = getBlackDuckUrl().appendRelativeUrl("api/current-user");
        RequestBuilder requestBuilder = RequestBuilder.get()
            .addHeader(AuthenticationSupport.AUTHORIZATION_HEADER, "Bearer " + bearerToken);
        return authenticationSupport.attemptAuthentication(this, currentUserUrl, requestBuilder);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        // no-op: Authorization: Bearer header is already set as a common header in the constructor
    }
}
