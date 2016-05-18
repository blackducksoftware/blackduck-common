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

import org.apache.commons.lang3.StringUtils;

public enum LogLevel {
	OFF(0), ERROR(1), WARN(2), INFO(3), DEBUG(4), TRACE(5);

	private int priority;

	private LogLevel(final int priority) {
		this.priority = priority;
	}

	private int getPriority() {
		return priority;
	}

	/**
	 * Will return true if the message is loggable at the current logger level. False otherwise.
	 * Order : Error, Warn, Info, Debug, Trace
	 *
	 */
	public static boolean isLoggable(final LogLevel logger, final LogLevel message) {
		// If the logger is set to TRACE(5) then all messages should be printed
		// If the logger is set to ERROR(1) then only ERROR messages will be printed
		// If the logger is set to INFO(3) and the message is DEBUG(4) then it wont be printed (3 is not >= 4).
		// If the logger is set to INFO(3) and the message is ERROR(1) then it will be printed (3 is >= 1).
		// If the logger is set to OFF(0), then no messages should be printed

		if (logger.getPriority() == 0) {
			return false;
		}

		if (logger.getPriority() >= message.getPriority()) {
			return true;
		}
		return false;

	}

	public static LogLevel fromString(final String level) {
		if (StringUtils.isNotBlank(level)) {
			try {
				return LogLevel.valueOf(level.toUpperCase());
			} catch (final IllegalArgumentException e) {
			}
		}
		return LogLevel.INFO;
	}
}
