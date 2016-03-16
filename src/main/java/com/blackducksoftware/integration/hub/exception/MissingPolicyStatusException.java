package com.blackducksoftware.integration.hub.exception;

public class MissingPolicyStatusException extends Exception {

    public MissingPolicyStatusException() {

    }

    public MissingPolicyStatusException(String message)
    {
        super(message);
    }

    public MissingPolicyStatusException(Throwable cause)
    {
        super(cause);
    }

    public MissingPolicyStatusException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
