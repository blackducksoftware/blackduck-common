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
package com.blackducksoftware.integration.hub.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class TestLogger extends IntLogger {
	private List<String> outputList = new ArrayList<>();
	private List<Throwable> errorList = new ArrayList<>();
	private LogLevel logLevel = LogLevel.TRACE;

	public List<String> getOutputList() {
		return outputList;
	}

	public List<Throwable> getErrorList() {
		return errorList;
	}

	public void resetOutputList() {
		outputList = new ArrayList<>();
	}

	public void resetErrorList() {
		errorList = new ArrayList<>();
	}

	public void resetAllOutput() {
		resetOutputList();
		resetErrorList();
	}

	public String getOutputString() {
		return StringUtils.join(outputList, System.getProperty("line.separator"));
	}

	public String getErrorOutputString() {
		if (null == errorList || errorList.isEmpty()) {
			return "";
		}

		final List<String> stackTraces = new ArrayList<>();
		for (final Throwable e : errorList) {
			final StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			stackTraces.add(stringWriter.toString());
		}

		return StringUtils.join(stackTraces, System.getProperty("line.separator"));
	}

	@Override
	public void debug(final String txt) {
		System.out.println(String.format("debug: %s", txt));
		outputList.add(txt);
	}

	@Override
	public void debug(final String txt, final Throwable e) {
		System.out.println(String.format("debug: %s, exception: %s", txt, e.getMessage()));
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void error(final Throwable e) {
		System.out.println(String.format("error: exception: %s", e.getMessage()));
		errorList.add(e);
	}

	@Override
	public void error(final String txt) {
		System.out.println(String.format("error: %s", txt));
		outputList.add(txt);
	}

	@Override
	public void error(final String txt, final Throwable e) {
		System.out.println(String.format("error: %s, exception: %s", txt, e.getMessage()));
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void info(final String txt) {
		System.out.println(String.format("info: %s", txt));
		outputList.add(txt);
	}

	@Override
	public void trace(final String txt) {
		System.out.println(String.format("trace: %s", txt));
		outputList.add(txt);
	}

	@Override
	public void trace(final String txt, final Throwable e) {
		System.out.println(String.format("trace: %s, exception: %s", txt, e.getMessage()));
		outputList.add(txt);
		errorList.add(e);
	}

	@Override
	public void warn(final String txt) {
		System.out.println(String.format("warn: %s", txt));
		outputList.add(txt);
	}

	@Override
	public void setLogLevel(final LogLevel level) {
		logLevel = level;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

}
