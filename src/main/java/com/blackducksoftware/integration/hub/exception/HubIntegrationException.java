package com.blackducksoftware.integration.hub.exception;

public class HubIntegrationException extends Exception {

    public HubIntegrationException() {

    }

    public HubIntegrationException(String message)
    {
        super(message);
    }

    public HubIntegrationException(Throwable cause)
    {
        super(cause);
    }

    public HubIntegrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
