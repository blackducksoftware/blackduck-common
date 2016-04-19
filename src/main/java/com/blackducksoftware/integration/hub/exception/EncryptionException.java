package com.blackducksoftware.integration.hub.exception;

public class EncryptionException extends Exception {
	private static final long serialVersionUID = 1026414965559049728L;

	public EncryptionException(final String message) {
		super(message);
	}

	public EncryptionException(final Throwable cause) {
		super(cause);
	}

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
