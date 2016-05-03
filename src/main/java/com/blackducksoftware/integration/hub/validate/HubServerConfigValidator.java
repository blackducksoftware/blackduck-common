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

import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfigBuilder;

public abstract class HubServerConfigValidator<T> extends AbstractValidator<T> {

	public T validateServerUrl(final String serverUrl) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setHubUrl(serverUrl);
			builder.validateHubUrl(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateServerUrl(final String serverUrl, final HubProxyInfo proxyInfo) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setHubUrl(serverUrl);
			builder.setProxyInfo(proxyInfo);
			builder.validateHubUrl(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateTimeout(final String timeout) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setTimeout(timeout);
			builder.validateTimeout(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}
}
