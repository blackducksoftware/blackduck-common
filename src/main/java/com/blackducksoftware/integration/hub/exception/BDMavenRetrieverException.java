package com.blackducksoftware.integration.hub.exception;

public class BDMavenRetrieverException extends Exception {

	private static final long serialVersionUID = -1L;

	public BDMavenRetrieverException() {

	}

	public BDMavenRetrieverException(final String message)
	{
		super(message);
	}

	public BDMavenRetrieverException(final Throwable cause)
	{
		super(cause);
	}

	public BDMavenRetrieverException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
