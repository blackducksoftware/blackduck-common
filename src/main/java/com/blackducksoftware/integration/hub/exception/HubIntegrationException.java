package com.blackducksoftware.integration.hub.exception;

public class HubIntegrationException extends Exception {
	private static final long serialVersionUID = 1L;

	public HubIntegrationException() {

	}

	public HubIntegrationException(final String message)
	{
		super(message);
	}

	public HubIntegrationException(final Throwable cause)
	{
		super(cause);
	}

	public HubIntegrationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
