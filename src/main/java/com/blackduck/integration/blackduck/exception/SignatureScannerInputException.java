/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.exception;

import com.blackduck.integration.exception.IntegrationException;

public class SignatureScannerInputException extends IntegrationException {
    public SignatureScannerInputException() {
        super();
    }

    public SignatureScannerInputException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SignatureScannerInputException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SignatureScannerInputException(final String message) {
        super(message);
    }

    public SignatureScannerInputException(final Throwable cause) {
        super(cause);
    }
}
