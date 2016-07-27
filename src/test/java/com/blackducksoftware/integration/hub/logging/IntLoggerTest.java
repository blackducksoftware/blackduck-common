package com.blackducksoftware.integration.hub.logging;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.CIEnvironmentVariables;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class IntLoggerTest {

	@Test
	public void testSetLogLevelWithVariables() {
		final TestLogger logger = new TestLogger();
		final CIEnvironmentVariables variables = new CIEnvironmentVariables();
		logger.setLogLevel(variables);
		assertEquals(LogLevel.INFO, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "FAKE");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.INFO, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "error");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.ERROR, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "erRor");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.ERROR, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "OFF");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.OFF, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "ERROR");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.ERROR, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "WARN");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.WARN, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "INFO");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.INFO, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "DEBUG");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.DEBUG, logger.getLogLevel());

		variables.put("HUB_LOG_LEVEL", "TRACE");
		logger.setLogLevel(variables);
		assertEquals(LogLevel.TRACE, logger.getLogLevel());
	}
}
