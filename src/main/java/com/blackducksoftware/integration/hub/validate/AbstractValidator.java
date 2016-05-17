/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
