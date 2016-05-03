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
package com.blackducksoftware.integration.hub.validate;

import java.io.IOException;

import com.blackducksoftware.integration.hub.global.HubProxyInfoBuilder;

public abstract class HubProxyInfoValidator<T> extends AbstractValidator<T> {

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
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setPort(port);
			builder.validatePort(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateCredentials(final String host, final String username, final String password) throws IOException {
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setUsername(username);
			builder.setPassword(password);
			builder.validateCredentials(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateIgnoreHosts(final String host, final String ignoreHosts) throws IOException {
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setIgnoredProxyHosts(ignoreHosts);
			builder.validateIgnoreHosts(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}
}
