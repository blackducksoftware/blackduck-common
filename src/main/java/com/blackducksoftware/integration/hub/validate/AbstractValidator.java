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
package com.blackducksoftware.integration.hub.validate;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.logging.IntBufferedLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public abstract class AbstractValidator<T> {

	private final IntBufferedLogger validationLogger = new IntBufferedLogger();

	public abstract T processResult(ValidationResult result);

	public ValidationResult handleValidationException(final Throwable t) {

		return new ValidationResult(ValidationResultEnum.ERROR, t.getMessage());
	}

	private ValidationResult handleErrors() {
		final String warnings = getValidationLogger().getOutputString(LogLevel.WARN);
		final String errors = getValidationLogger().getOutputString(LogLevel.ERROR);

		if (StringUtils.isNotBlank(errors)) {
			return new ValidationResult(ValidationResultEnum.ERROR, errors);
		} else {
			return new ValidationResult(ValidationResultEnum.WARN, warnings);
		}
	}

	public boolean hasErrors() {
		return !validationLogger.getOutputList(LogLevel.WARN).isEmpty()
				|| !validationLogger.getOutputList(LogLevel.ERROR).isEmpty();
	}

	public IntBufferedLogger getValidationLogger() {
		return validationLogger;
	}

	public ValidationResult createResult() {

		final ValidationResult result;
		if (hasErrors()) {
			result = handleErrors();
		} else {
			result = new ValidationResult(ValidationResultEnum.OK, "");
		}

		validationLogger.resetAllLogs();

		return result;
	}
}
