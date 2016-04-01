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
