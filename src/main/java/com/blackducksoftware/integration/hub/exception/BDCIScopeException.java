package com.blackducksoftware.integration.hub.exception;

public class BDCIScopeException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1L;

	public BDCIScopeException() {

    }

    public BDCIScopeException(String message)
    {
        super(message);
    }

    public BDCIScopeException(Throwable cause)
    {
        super(cause);
    }

    public BDCIScopeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
