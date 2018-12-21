package com.synopsys.integration.blackduck.api.component;

import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;

public class ErrorResponse extends BlackDuckComponent {
    private final String errorMessage;
    private final String errorCode;

    public ErrorResponse(final String errorMessage, final String errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
