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
