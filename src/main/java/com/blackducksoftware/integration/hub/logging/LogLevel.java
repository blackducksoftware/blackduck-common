package com.blackducksoftware.integration.hub.logging;

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
}
