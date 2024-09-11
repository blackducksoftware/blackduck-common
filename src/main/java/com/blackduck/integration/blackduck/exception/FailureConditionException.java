/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.exception;

public class FailureConditionException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public FailureConditionException() {
        super();
    }

    public FailureConditionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FailureConditionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FailureConditionException(final String message) {
        super(message);
    }

    public FailureConditionException(final Throwable cause) {
        super(cause);
    }

}
