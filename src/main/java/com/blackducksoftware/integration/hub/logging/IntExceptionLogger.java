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
package com.blackducksoftware.integration.hub.logging;

import com.blackducksoftware.integration.hub.ValidationExceptionEnum;
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
		throw new ValidationException(ValidationExceptionEnum.ERROR, t);
	}

	@Override
	public void error(final String txt, final Throwable t) {
		throw new ValidationException(ValidationExceptionEnum.ERROR, txt, t);
	}

	@Override
	public void error(final String txt) {
		throw new ValidationException(ValidationExceptionEnum.ERROR, txt);
	}

	@Override
	public void warn(final String txt) {
		throw new ValidationException(ValidationExceptionEnum.WARN, txt);
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
