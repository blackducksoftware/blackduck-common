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
package com.blackducksoftware.integration.hub.global;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubCredentialsBuilder {
	private String username;
	private String password;

	public HubCredentials build(final IntLogger logger)
			throws HubIntegrationException, IllegalArgumentException, EncryptionException {
		assertValid(logger);

		return new HubCredentials(username, password);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException {
		boolean valid = true;

		if (!validateCredentials(logger)) {
			valid = false;
		}

		if (!valid) {
			throw new HubIntegrationException(
					"The credentials are not valid - please check the log for the specific issues.");
		}
	}

	public boolean validateCredentials(final IntLogger logger) {
		boolean valid = true;

		if (!validateUsername(logger)) {
			valid = false;
		}

		if (!validatePassword(logger)) {
			valid = false;
		}

		return valid;
	}

	public boolean validateUsername(final IntLogger logger) {
		boolean valid = true;
		if (StringUtils.isBlank(username)) {
			valid = false;
			logger.error("No Hub Username was found.");
		}
		return valid;
	}

	public boolean validatePassword(final IntLogger logger) {
		boolean valid = true;
		if (StringUtils.isBlank(password)) {
			valid = false;
			logger.error("No Hub Password was found.");
		}
		return valid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

}
