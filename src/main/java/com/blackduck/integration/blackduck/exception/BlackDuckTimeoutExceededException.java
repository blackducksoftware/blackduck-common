/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.exception;

public class BlackDuckTimeoutExceededException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public BlackDuckTimeoutExceededException() {
    }

    public BlackDuckTimeoutExceededException(final String message) {
        super(message);
    }

    public BlackDuckTimeoutExceededException(final Throwable cause) {
        super(cause);
    }

    public BlackDuckTimeoutExceededException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
