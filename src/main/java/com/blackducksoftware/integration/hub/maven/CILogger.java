package com.blackducksoftware.integration.hub.maven;

import org.slf4j.Logger;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

/**
 * This logger class is meant to simplify the logging the Common code project. This logger will not be particularly
 * useful in other projects.
 *
 * @author jrichard
 *
 */
public class CILogger {

	private Logger logger;

	private IntLogger intLogger;

	protected CILogger(final IntLogger intLogger, final Logger logger) {
		this.intLogger = intLogger;
		this.logger = logger;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(final Logger logger) {
		this.logger = logger;
	}

	public IntLogger getIntLogger() {
		return intLogger;
	}

	public void setIntLogger(final IntLogger intLogger) {
		this.intLogger = intLogger;
	}

	public void info(final String txt) {
		if (intLogger != null) {
			intLogger.info(txt);
		} else if (logger != null) {
			logger.info(txt);
		}

	}

	public void error(final Throwable e) {
		if (intLogger != null) {
			intLogger.error(e);
		} else if (logger != null) {
			logger.error(e.getMessage(), e);
		}

	}

	public void error(final String txt, final Throwable e) {
		if (intLogger != null) {
			intLogger.error(txt, e);
		} else if (logger != null) {
			logger.error(txt, e);
		}

	}

	public void error(final String txt) {
		if (intLogger != null) {
			intLogger.error(txt);
		} else if (logger != null) {
			logger.error(txt);
		}

	}

	public void warn(final String txt) {
		if (intLogger != null) {
			intLogger.warn(txt);
		} else if (logger != null) {
			logger.warn(txt);
		}

	}

	public void trace(final String txt) {
		if (intLogger != null) {
			intLogger.trace(txt);
		} else if (logger != null) {
			logger.trace(txt);
		}

	}

	public void trace(final String txt, final Throwable e) {
		if (intLogger != null) {
			intLogger.trace(txt, e);
		} else if (logger != null) {
			logger.trace(txt, e);
		}
	}

	public void debug(final String txt) {
		if (intLogger != null) {
			intLogger.debug(txt);
		} else if (logger != null) {
			logger.debug(txt);
		}

	}

	public void debug(final String txt, final Throwable e) {
		if (intLogger != null) {
			intLogger.debug(txt, e);
		} else if (logger != null) {
			logger.debug(txt, e);
		}

	}

	public void setLogLevel(final LogLevel logLevel) {
		if (intLogger != null) {
			intLogger.setLogLevel(logLevel);
		}
	}

	public LogLevel getLogLevel() {
		if (intLogger != null) {
			return intLogger.getLogLevel();
		}
		return null;
	}
}
