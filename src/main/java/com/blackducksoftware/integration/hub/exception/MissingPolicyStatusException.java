package com.blackducksoftware.integration.hub.exception;

public class MissingPolicyStatusException extends Exception {
	private static final long serialVersionUID = 1L;

	public MissingPolicyStatusException() {

	}

	public MissingPolicyStatusException(final String message)
	{
		super(message);
	}

	public MissingPolicyStatusException(final Throwable cause)
	{
		super(cause);
	}

	public MissingPolicyStatusException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
