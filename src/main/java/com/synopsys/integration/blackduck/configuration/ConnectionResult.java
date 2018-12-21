package com.synopsys.integration.blackduck.configuration;

import java.util.Optional;

public class ConnectionResult {
    private final String errorMessage;

    public static final ConnectionResult SUCCESS() {
        return new ConnectionResult(null);
    }

    public static final ConnectionResult FAILURE(final String errorMessage) {
        return new ConnectionResult(errorMessage);
    }

    private ConnectionResult(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return !isFailure();
    }

    public boolean isFailure() {
        return getErrorMessage().isPresent();
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

}
