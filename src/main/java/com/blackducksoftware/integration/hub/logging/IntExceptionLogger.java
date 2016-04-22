package com.blackducksoftware.integration.hub.logging;

import com.blackducksoftware.integration.hub.ValidationMessageEnum;
import com.blackducksoftware.integration.hub.exception.ValidationException;

/**
 * This logger will only have implementation for the error() and warn()
 * invocations, which should all simply throw a ValidationException with the
 * appropriate ValidationMessageEnum.
 */
public class IntExceptionLogger implements IntLogger {
	public static final IntLogger LOGGER = new IntExceptionLogger();

	@Override
	public void info(final String txt) {
	}

	@Override
	public void error(final Throwable t) {
		throw new ValidationException(ValidationMessageEnum.ERROR, t);
	}

	@Override
	public void error(final String txt, final Throwable t) {
		throw new ValidationException(ValidationMessageEnum.ERROR, txt, t);
	}

	@Override
	public void error(final String txt) {
		throw new ValidationException(ValidationMessageEnum.ERROR, txt);
	}

	@Override
	public void warn(final String txt) {
		throw new ValidationException(ValidationMessageEnum.WARN, txt);
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
	}

	@Override
	public LogLevel getLogLevel() {
		return null;
	}

}
