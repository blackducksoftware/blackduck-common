/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.configuration;

import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.rest.client.ConnectionResult;

import java.util.Optional;

public class BlackDuckConnectionResult extends ConnectionResult {
    public static final BlackDuckConnectionResult BLACK_DUCK_SUCCESS(int httpStatusCode, BlackDuckHttpClient blackDuckHttpClient) {
        return new BlackDuckConnectionResult(httpStatusCode, null, null, blackDuckHttpClient);
    }
    public static final BlackDuckConnectionResult BLACK_DUCK_FAILURE(int httpStatusCode, String failureMessage, Exception exception) {
        return new BlackDuckConnectionResult(httpStatusCode, failureMessage, exception, null);
    }

    private final BlackDuckHttpClient blackDuckHttpClient;

    public BlackDuckConnectionResult(int httpStatusCode, String failureMessage, Exception exception, BlackDuckHttpClient blackDuckHttpClient) {
        super(httpStatusCode, failureMessage, exception);
        this.blackDuckHttpClient = blackDuckHttpClient;
    }

    public Optional<BlackDuckHttpClient> getBlackDuckHttpClient() {
        return Optional.ofNullable(blackDuckHttpClient);
    }

}
