/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.exception;

public class MismatchedQuotesException extends SignatureScannerInputException {
    public MismatchedQuotesException() {
        super();
    }

    public MismatchedQuotesException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MismatchedQuotesException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MismatchedQuotesException(final String message) {
        super(message);
    }

    public MismatchedQuotesException(final Throwable cause) {
        super(cause);
    }
}
