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

import java.util.HashMap;
import java.util.Map;

/**
 * Connection to the Black Duck application which authenticates using the API token feature
 */
public class ApiTokenBlackDuckHttpClient extends DefaultBlackDuckHttpClient {
    private final String apiToken;

    public ApiTokenBlackDuckHttpClient(
        IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, String apiToken) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, solutionDetails, authenticationSupport);
        this.apiToken = apiToken;

        if (StringUtils.isBlank(apiToken)) {
            throw new IllegalArgumentException("No API token was found.");
        }
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        Map<String, String> headers = new HashMap<>();
        headers.put(AuthenticationSupport.AUTHORIZATION_HEADER, "token " + apiToken);

        return authenticationSupport.attemptAuthentication(this, getBlackDuckUrl(), "api/tokens/authenticate", headers);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        authenticationSupport.completeTokenAuthenticationRequest(request, response, logger, getGson(), this, "bearerToken");
    }

}
