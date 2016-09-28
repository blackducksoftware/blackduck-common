/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
	public ValidationResults<GlobalFieldKey, HubCredentials> buildResults() {
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
		final ValidationResults<GlobalFieldKey, HubCredentials> result = new ValidationResults<>();

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

	/**
	 * IMPORTANT : The password length should only be set if the password is
	 * already encrypted
	 */
	public void setPasswordLength(final int passwordLength) {
		this.passwordLength = passwordLength;
	}

}
