package com.blackducksoftware.integration.hub.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class BufferedLogger implements IntLogger {
	private static final Map<LogLevel, List<String>> outputMap = new HashMap<LogLevel, List<String>>();

	static {
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
		return StringUtils.join(outputMap.get(level), '\n');
	}

	public String getErrorOutputString(final Throwable e) {
		final StringBuilder sb = new StringBuilder();
		if (sb.length() > 0) {
			sb.append('\n');
		}
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		sb.append(sw.toString());
		return sb.toString();
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
