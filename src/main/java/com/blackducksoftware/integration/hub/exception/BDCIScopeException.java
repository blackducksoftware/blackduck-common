package com.blackducksoftware.integration.hub.exception;

public class BDCIScopeException extends Exception {

	private static final long serialVersionUID = -1L;

	public BDCIScopeException() {

	}

	public BDCIScopeException(final String message)
	{
		super(message);
	}

	public BDCIScopeException(final Throwable cause)
	{
		super(cause);
	}

	public BDCIScopeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
