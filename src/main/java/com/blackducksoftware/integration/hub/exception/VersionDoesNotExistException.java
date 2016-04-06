package com.blackducksoftware.integration.hub.exception;

public class VersionDoesNotExistException extends Exception {
	private static final long serialVersionUID = 1L;


	public VersionDoesNotExistException(final String message)
	{
		super(message);
	}

	public VersionDoesNotExistException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
