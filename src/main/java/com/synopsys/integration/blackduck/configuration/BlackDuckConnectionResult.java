package com.synopsys.integration.blackduck.configuration;

import java.util.Optional;

import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.rest.client.ConnectionResult;

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
