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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class IntBufferedLogger implements IntLogger {
	private final Map<LogLevel, List<String>> outputMap = new HashMap<LogLevel, List<String>>();


	public IntBufferedLogger(){
		outputMap.put(LogLevel.ERROR, new ArrayList<String>());
		outputMap.put(LogLevel.WARN, new ArrayList<String>());
		outputMap.put(LogLevel.INFO, new ArrayList<String>());
		outputMap.put(LogLevel.DEBUG, new ArrayList<String>());
		outputMap.put(LogLevel.TRACE, new ArrayList<String>());
	}

	public void resetLogs(final LogLevel level){
		outputMap.put(level, new ArrayList<String>());
	}

	public void resetAllLogs(){
		outputMap.put(LogLevel.ERROR, new ArrayList<String>());
		outputMap.put(LogLevel.WARN, new ArrayList<String>());
		outputMap.put(LogLevel.INFO, new ArrayList<String>());
		outputMap.put(LogLevel.DEBUG, new ArrayList<String>());
		outputMap.put(LogLevel.TRACE, new ArrayList<String>());
	}

	public List<String> getOutputList(final LogLevel level) {
		return outputMap.get(level);
	}

	public String getOutputString(final LogLevel level) {
		return StringUtils.trimToNull(StringUtils.join(outputMap.get(level), '\n'));
	}

	public String getErrorOutputString(final Throwable e) {
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	@Override
	public void debug(final String txt) {
		outputMap.get(LogLevel.DEBUG).add(txt);
	}

	@Override
	public void debug(final String txt, final Throwable e) {
		outputMap.get(LogLevel.DEBUG).add(txt);
		outputMap.get(LogLevel.DEBUG).add(getErrorOutputString(e));
	}

	@Override
	public void error(final Throwable e) {
		outputMap.get(LogLevel.ERROR).add(getErrorOutputString(e));
	}

	@Override
	public void error(final String txt) {
		outputMap.get(LogLevel.ERROR).add(txt);
	}

	@Override
	public void error(final String txt, final Throwable e) {
		outputMap.get(LogLevel.ERROR).add(txt);
		outputMap.get(LogLevel.ERROR).add(getErrorOutputString(e));
	}

	@Override
	public void info(final String txt) {
		outputMap.get(LogLevel.INFO).add(txt);
	}

	@Override
	public void trace(final String txt) {
		outputMap.get(LogLevel.TRACE).add(txt);
	}

	@Override
	public void trace(final String txt, final Throwable e) {
		outputMap.get(LogLevel.TRACE).add(txt);
		outputMap.get(LogLevel.TRACE).add(getErrorOutputString(e));
	}

	@Override
	public void warn(final String txt) {
		outputMap.get(LogLevel.WARN).add(txt);
	}

	@Override
	public void setLogLevel(final LogLevel level) {
	}

	@Override
	public LogLevel getLogLevel() {
		return null;
	}

}
