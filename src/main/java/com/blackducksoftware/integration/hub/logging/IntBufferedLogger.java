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
