package com.blackducksoftware.integration.hub.exception;

public class UnexpectedHubResponseException extends Exception {
	private static final long serialVersionUID = -6736510347450951980L;

	public UnexpectedHubResponseException() {
	}

	public UnexpectedHubResponseException(final String message) {
		super(message);
	}

	public UnexpectedHubResponseException(final Throwable cause) {
		super(cause);
	}

	public UnexpectedHubResponseException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
