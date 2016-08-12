package com.blackducksoftware.integration.hub.exception;

public class HubItemTransformException extends Exception {
	private static final long serialVersionUID = 725228235172767571L;

	public HubItemTransformException() {
	}

	public HubItemTransformException(final String message) {
		super(message);
	}

	public HubItemTransformException(final Throwable cause) {
		super(cause);
	}

	public HubItemTransformException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
