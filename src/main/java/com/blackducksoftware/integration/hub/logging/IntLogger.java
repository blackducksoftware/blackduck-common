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

import com.blackducksoftware.integration.hub.CIEnvironmentVariables;

public abstract class IntLogger {
	public abstract void info(String txt);

	public abstract void error(Throwable t);

	public abstract void error(String txt, Throwable t);

	public abstract void error(String txt);

	public abstract void warn(String txt);

	public abstract void trace(String txt);

	public abstract void trace(String txt, Throwable t);

	public abstract void debug(String txt);

	public abstract void debug(String txt, Throwable t);

	public abstract void setLogLevel(LogLevel logLevel);

	public void setLogLevel(final CIEnvironmentVariables variables) {
		final String logLevel = variables.getValue("HUB_LOG_LEVEL", "INFO");
		try {
			setLogLevel(LogLevel.valueOf(logLevel.toUpperCase()));
		} catch (final IllegalArgumentException e) {
			setLogLevel(LogLevel.INFO);
		}
	}

	public abstract LogLevel getLogLevel();

}
