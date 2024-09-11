/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.exception;

public class ScanFailedException extends BlackDuckIntegrationException {
    private static final long serialVersionUID = 1L;

    public ScanFailedException() {
        super();
    }

    public ScanFailedException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ScanFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ScanFailedException(final String message) {
        super(message);
    }

    public ScanFailedException(final Throwable cause) {
        super(cause);
    }

}
