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

public interface IntLogger {
	void info(String txt);

	void error(Throwable e);

	void error(String txt, Throwable t);

	void error(String txt);

	void warn(String txt);

	void trace(String txt);

	void trace(String txt, Throwable t);

	void debug(String txt);

	void debug(String txt, Throwable t);

	void setLogLevel(LogLevel logLevel);

	LogLevel getLogLevel();

}
