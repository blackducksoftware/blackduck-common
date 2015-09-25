package com.blackducksoftware.integration.hub.exception;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class BDRestException extends Exception {

    private ResourceException resourceEx;

    private ClientResource resource;

    public BDRestException() {

    }

    public BDRestException(String message)
    {
        super(message);
    }

    public BDRestException(String message, ClientResource resource)
    {
        super(message);
        this.resource = resource;
    }

    public BDRestException(Throwable cause)
    {
        super(cause);
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public BDRestException(Throwable cause, ClientResource resource)
    {
        super(cause);
        this.resource = resource;
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public BDRestException(String message, Throwable cause)
    {
        super(message, cause);
        if (cause instanceof ResourceException) {
            resourceEx = (ResourceException) cause;
        }
    }

    public BDRestException(String message, Throwable cause, ClientResource resource)
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
