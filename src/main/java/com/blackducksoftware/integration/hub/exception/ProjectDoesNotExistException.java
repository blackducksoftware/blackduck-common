package com.blackducksoftware.integration.hub.exception;

import org.restlet.resource.ClientResource;

public class ProjectDoesNotExistException extends Exception {

    private final ClientResource resource;

    public ProjectDoesNotExistException(String message, ClientResource resource)
    {
        super(message);
        this.resource = resource;
    }

    public ProjectDoesNotExistException(String message, Throwable cause, ClientResource resource)
    {
        super(message, cause);
        this.resource = resource;
    }

    public ClientResource getResource() {
        return resource;
    }
}
