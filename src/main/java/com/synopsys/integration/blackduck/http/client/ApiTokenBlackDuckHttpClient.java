/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.NameVersion;

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
