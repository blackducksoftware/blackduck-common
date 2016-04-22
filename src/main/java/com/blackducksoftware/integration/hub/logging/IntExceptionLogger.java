package com.blackducksoftware.integration.hub.logging;

import com.blackducksoftware.integration.hub.exception.HubIntegrationRuntimeException;

/**
 * This logger will only have implementation for the error() invocations, which
 * should all simply throw a RuntimeException with the appropriate details.
 */
public class IntExceptionLogger implements IntLogger {
	public static final IntLogger LOGGER = new IntExceptionLogger();

	private LogLevel logLevel = LogLevel.ERROR;

	@Override
	public void info(final String txt) {
	}

	@Override
	public void error(final Throwable t) {
		throw new HubIntegrationRuntimeException(t);
	}

	@Override
	public void error(final String txt, final Throwable t) {
		throw new HubIntegrationRuntimeException(txt, t);
	}

	@Override
	public void error(final String txt) {
		throw new HubIntegrationRuntimeException(txt);
	}

	@Override
	public void warn(final String txt) {
	}

	@Override
	public void trace(final String txt) {
	}

	@Override
	public void trace(final String txt, final Throwable t) {
	}

	@Override
	public void debug(final String txt) {
	}

	@Override
	public void debug(final String txt, final Throwable t) {
	}

	@Override
	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

}
