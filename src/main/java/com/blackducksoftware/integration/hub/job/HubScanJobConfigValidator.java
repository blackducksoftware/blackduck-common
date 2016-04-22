package com.blackducksoftware.integration.hub.job;

import java.io.IOException;

import com.blackducksoftware.integration.hub.exception.ValidationException;
import com.blackducksoftware.integration.hub.logging.IntExceptionLogger;

public abstract class HubScanJobConfigValidator<T> {
	public abstract T handleValidationException(ValidationException e);

	public abstract T handleSuccess();

	public T validateScanMemory(final String scanMemory) throws IOException {
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setScanMemory(scanMemory);
			builder.validateScanMemory(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		}

		return handleSuccess();
	}

	public T validateMaxWaitTimeForBomUpdate(final String bomUpdateMaxiumWaitTime) throws IOException {
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setMaxWaitTimeForBomUpdate(bomUpdateMaxiumWaitTime);
			builder.validateMaxWaitTimeForBomUpdate(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		}

		return handleSuccess();
	}

}
