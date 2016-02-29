package com.blackducksoftware.integration.hub.exception;

import org.restlet.resource.ClientResource;

public class BDRestException extends Exception {

    private final ClientResource resource;

    public BDRestException(String message, ClientResource resource)
    {
        super(message);
        this.resource = resource;
    }

    public BDRestException(String message, Throwable cause, ClientResource resource)
    {
        super(message, cause);
        this.resource = resource;
    }

    public ClientResource getResource() {
        return resource;
    }
}
