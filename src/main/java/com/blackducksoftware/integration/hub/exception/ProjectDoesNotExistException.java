package com.blackducksoftware.integration.hub.exception;

import org.restlet.resource.ClientResource;

public class ProjectDoesNotExistException extends Exception {
	private static final long serialVersionUID = 1L;

	private final ClientResource resource;

	public ProjectDoesNotExistException(final String message, final ClientResource resource)
	{
		super(message);
		this.resource = resource;
	}

	public ProjectDoesNotExistException(final String message, final Throwable cause, final ClientResource resource)
	{
		super(message, cause);
		this.resource = resource;
	}

	public ClientResource getResource() {
		return resource;
	}
}
