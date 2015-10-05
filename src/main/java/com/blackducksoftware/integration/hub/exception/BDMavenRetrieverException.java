package com.blackducksoftware.integration.hub.exception;

public class BDMavenRetrieverException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1L;

	public BDMavenRetrieverException() {

    }

    public BDMavenRetrieverException(String message)
    {
        super(message);
    }

    public BDMavenRetrieverException(Throwable cause)
    {
        super(cause);
    }

    public BDMavenRetrieverException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
