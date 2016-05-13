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
