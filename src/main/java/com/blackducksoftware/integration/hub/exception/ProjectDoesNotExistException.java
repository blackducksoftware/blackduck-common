package com.blackducksoftware.integration.hub.exception;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class ProjectDoesNotExistException extends Exception {

    private ResourceException resourceEx;

    private ClientResource resource;

    public ProjectDoesNotExistException() {

    }

    public ProjectDoesNotExistException(String message)
    {
        super(message);
    }

    public ProjectDoesNotExistException(String message, ClientResource resource)
    {
        super(message);
        this.resource = resource;
    }

    public ProjectDoesNotExistException(Throwable cause)
    {
        super(cause);
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public ProjectDoesNotExistException(Throwable cause, ClientResource resource)
    {
        super(cause);
        this.resource = resource;
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public ProjectDoesNotExistException(String message, Throwable cause)
    {
        super(message, cause);
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public ProjectDoesNotExistException(String message, Throwable cause, ClientResource resource)
    {
        super(message, cause);
        this.resource = resource;
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public ResourceException getResourceException() {
        return resourceEx;
    }

    public ClientResource getResource() {
        return resource;
    }
}
