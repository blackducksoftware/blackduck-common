/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.maven;

import org.slf4j.Logger;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

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
