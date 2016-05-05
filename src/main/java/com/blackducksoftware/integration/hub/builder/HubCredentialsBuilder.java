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

public class HubCredentialsBuilder extends AbstractBuilder<String, HubCredentials> {

	private String username;
	private String password;

	public HubCredentialsBuilder() {
		super(false);
	}

	public HubCredentialsBuilder(final boolean eatExceptionsOnSetters) {
		super(eatExceptionsOnSetters);
	}

	@Override
	public ValidationResults<String, HubCredentials> build() {
		final ValidationResults<String, HubCredentials> result = assertValid();
		String encryptedPassword = null;
		try {
			encryptedPassword = PasswordEncrypter.encrypt(password);
		} catch (final EncryptionException e) {
			e.printStackTrace();
		}
		final HubCredentials creds = new HubCredentials(username, encryptedPassword, password.length());
		result.setConstructedObject(creds);
		return result;
	}

	@Override
	public ValidationResults<String, HubCredentials> assertValid() {
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();

		validateCredentials(result);

		return result;
	}

	public void validateCredentials(final ValidationResults<String, HubCredentials> result) {

		validateUsername(result);
		validatePassword(result);

	}

	public boolean validateUsername(final ValidationResults<String, HubCredentials> result) {
		boolean valid = true;
		if (StringUtils.isBlank(username)) {
			valid = false;
			result.addResult("hubUserName",
					new ValidationResult(ValidationResultEnum.ERROR, "No Hub Username was found."));
		} else {
			result.addResult("hubUserName", new ValidationResult(ValidationResultEnum.OK, ""));
		}
		return valid;
	}

	public boolean validatePassword(final ValidationResults<String, HubCredentials> result) {
		boolean valid = true;
		if (StringUtils.isBlank(password)) {
			valid = false;
			result.addResult("hubPassword",
					new ValidationResult(ValidationResultEnum.ERROR, "No Hub Password was found."));
		} else {
			result.addResult("hubPassword", new ValidationResult(ValidationResultEnum.OK, ""));
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
