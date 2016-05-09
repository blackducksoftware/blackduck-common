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
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;

public class HubCredentialsBuilder extends AbstractBuilder<GlobalFieldKey, HubCredentials> {

	private String username;
	private String password;
	private int passwordLength;

	public HubCredentialsBuilder() {
		super(false);
	}

	public HubCredentialsBuilder(final boolean shouldUseDefaultValues) {
		super(shouldUseDefaultValues);
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubCredentials> build() {
		final ValidationResults<GlobalFieldKey, HubCredentials> result = assertValid();
		HubCredentials creds = null;
		if (StringUtils.isNotBlank(password) && passwordLength == 0) {
			// Password needs to be encrypted
			String encryptedPassword = null;
			try {
				encryptedPassword = PasswordEncrypter.encrypt(password);
			} catch (final EncryptionException e) {
				result.addResult(HubCredentialsFieldEnum.PASSWORD,
						new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
			}
			creds = new HubCredentials(username, encryptedPassword, password.length());
		} else {
			// password is blank or already encrypted so we just pass in the
			// values given to us
			creds = new HubCredentials(username, password, passwordLength);
		}

		result.setConstructedObject(creds);
		return result;
	}

	@Override
	public ValidationResults<GlobalFieldKey, HubCredentials> assertValid() {
		final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<GlobalFieldKey, HubCredentials>();

		validateCredentials(result);

		return result;
	}

	public void validateCredentials(final ValidationResults<GlobalFieldKey, HubCredentials> result) {

		validateUsername(result);
		validatePassword(result);
	}

	public void validateUsername(final ValidationResults<GlobalFieldKey, HubCredentials> result) {
		if (StringUtils.isBlank(username)) {
			result.addResult(HubCredentialsFieldEnum.USERNAME,
					new ValidationResult(ValidationResultEnum.ERROR, "No Hub Username was found."));
		} else {
			result.addResult(HubCredentialsFieldEnum.USERNAME, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validatePassword(final ValidationResults<GlobalFieldKey, HubCredentials> result) {
		if (StringUtils.isBlank(password)) {
			result.addResult(HubCredentialsFieldEnum.PASSWORD,
					new ValidationResult(ValidationResultEnum.ERROR, "No Hub Password was found."));
		} else {
			result.addResult(HubCredentialsFieldEnum.PASSWORD, new ValidationResult(ValidationResultEnum.OK, ""));
		}
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

	public int getPasswordLength() {
		return passwordLength;
	}

	public void setPasswordLength(final int passwordLength) {
		this.passwordLength = passwordLength;
	}

}
