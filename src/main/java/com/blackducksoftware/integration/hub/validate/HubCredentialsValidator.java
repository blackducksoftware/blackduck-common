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

import com.blackducksoftware.integration.hub.global.HubCredentialsBuilder;

public abstract class HubCredentialsValidator<T> extends AbstractValidator<T> {

	public T validateCredentials(final String username, final String password) throws IOException {
		ValidationResult result;
		try {
			final HubCredentialsBuilder builder = new HubCredentialsBuilder();
			builder.setUsername(username);
			builder.setPassword(password);
			builder.validateCredentials(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateUserName(final String username) throws IOException {
		ValidationResult result;
		try {
			final HubCredentialsBuilder builder = new HubCredentialsBuilder();
			builder.setUsername(username);
			builder.validateUsername(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}

		return processResult(result);
	}

	public T validatePassword(final String password) throws IOException {
		ValidationResult result;
		try {
			final HubCredentialsBuilder builder = new HubCredentialsBuilder();
			builder.setPassword(password);
			builder.validatePassword(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}

		return processResult(result);
	}

}
