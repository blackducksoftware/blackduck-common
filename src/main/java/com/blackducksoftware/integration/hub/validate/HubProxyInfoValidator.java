package com.blackducksoftware.integration.hub.validate;

import java.io.IOException;

import com.blackducksoftware.integration.hub.ValidationExceptionEnum;
import com.blackducksoftware.integration.hub.exception.ValidationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.logging.IntExceptionLogger;

public abstract class HubProxyInfoValidator<T> {

	public abstract T handleValidationException(ValidationException e);

	public abstract T handleSuccess();

	public T validatePort(final String host, final String port) throws IOException {
		int portValue = 0; // invalid port value
		try {
			portValue = Integer.valueOf(port);
		} catch (final Exception ex) {
			// the port is invalid leave it at 0
		}

		return validatePort(host, portValue);
	}

	public T validatePort(final String host, final int port) throws IOException {

		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setPort(port);
			builder.validatePort(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		} catch (final IllegalArgumentException e) {
			return handleValidationException(new ValidationException(ValidationExceptionEnum.ERROR, e.getMessage(), e));
		}

		return handleSuccess();
	}

	public T validateCredentials(final String host, final String username, final String password) throws IOException {
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setUsername(username);
			builder.setPassword(password);
			builder.validateCredentials(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		} catch (final IllegalArgumentException e) {
			return handleValidationException(new ValidationException(ValidationExceptionEnum.ERROR, e.getMessage(), e));
		}

		return handleSuccess();
	}

	public T validateIgnoreHosts(final String host, final String ignoreHosts) throws IOException {
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setIgnoredProxyHosts(ignoreHosts);
			builder.validateIgnoreHosts(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		} catch (final IllegalArgumentException e) {
			return handleValidationException(new ValidationException(ValidationExceptionEnum.ERROR, e.getMessage(), e));
		}

		return handleSuccess();
	}
}
