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
package com.blackducksoftware.integration.hub.builder;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.global.HubCredentials;

public class HubCredentialsBuilder extends AbstractBuilder {

	private String username;
	private String password;


	@Override
	public ValidationResult<HubCredentials> build() {
		final ValidationResult<HubCredentials> result = assertValid();
		String encryptedPassword = null;
		try {
			encryptedPassword = PasswordEncrypter.encrypt(password);
		} catch (final EncryptionException e) {
			e.printStackTrace();
		}
		new HubCredentials(username, encryptedPassword, password.length());
		return result;
	}

	@Override
	public ValidationResult<HubCredentials> assertValid() {
		final ValidationResult<HubCredentials> result = null;

		validateCredentials(result);

		// if (!valid) {
		// // throw new HubIntegrationException(
		// // "The credentials are not valid - please check the log for the
		// // specific issues.");
		// }
		return null;
	}

	public void validateCredentials(final ValidationResult<HubCredentials> result) {

		validateUsername(result);
		validatePassword(result);

	}

	public boolean validateUsername(final ValidationResult<HubCredentials> result) {
		boolean valid = true;
		if (StringUtils.isBlank(username)) {
			valid = false;
			logger.error("No Hub Username was found.");
		}
		return valid;
	}

	public boolean validatePassword(final ValidationResult<HubCredentials> result) {
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
