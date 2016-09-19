package com.blackducksoftware.integration.hub.logging;

import org.slf4j.Logger;

public class Slf4jIntLogger extends IntLogger {
	private final Logger logger;

	public Slf4jIntLogger(final Logger logger) {
		this.logger = logger;
	}

	@Override
	public void info(final String txt) {
		logger.info(txt);
	}

	@Override
	public void error(final Throwable t) {
		logger.error("Throwable: " + t.getMessage(), t);
	}

	@Override
	public void error(final String txt, final Throwable t) {
		logger.error(txt, t);
	}

	@Override
	public void error(final String txt) {
		logger.error(txt);
	}

	@Override
	public void warn(final String txt) {
		logger.warn(txt);
	}

	@Override
	public void trace(final String txt) {
		logger.trace(txt);
	}

	@Override
	public void trace(final String txt, final Throwable t) {
		logger.trace(txt, t);
	}

	@Override
	public void debug(final String txt) {
		logger.debug(txt);
	}

	@Override
	public void debug(final String txt, final Throwable t) {
		logger.debug(txt, t);
	}

	@Override
	public void setLogLevel(final LogLevel logLevel) {
	}

	@Override
	public LogLevel getLogLevel() {
		return null;
	}

}
