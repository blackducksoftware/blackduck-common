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
package com.blackducksoftware.integration.hub.global;

import java.io.Serializable;
import java.util.Arrays;

import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;

public class HubCredentials implements Serializable {
	private static final long serialVersionUID = 7924589951692259151L;

	private final String username;
	private final String encryptedPassword;
	private final int actualPasswordLength;

	public HubCredentials(final String username, final String password) throws EncryptionException {
		this.username = username;
		this.actualPasswordLength = (password == null ? 0 : password.length());
		this.encryptedPassword = PasswordEncrypter.encrypt(password);
	}

	public HubCredentials(final String username, final String encryptedPassword, final int actualPasswordLength) {
		this.username = username;
		this.actualPasswordLength = actualPasswordLength;
		this.encryptedPassword = encryptedPassword;
	}

	public String getMaskedPassword() {
		final char[] array = new char[actualPasswordLength];
		Arrays.fill(array, '*');
		return new String(array);
	}

	public String getDecryptedPassword() throws IllegalArgumentException, EncryptionException {
		return PasswordDecrypter.decrypt(encryptedPassword);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubCredentials [username=");
		builder.append(username);
		builder.append(", encryptedPassword=");
		builder.append(encryptedPassword);
		builder.append(", actualPasswordLength=");
		builder.append(actualPasswordLength);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actualPasswordLength;
		result = prime * result + ((encryptedPassword == null) ? 0 : encryptedPassword.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HubCredentials)) {
			return false;
		}
		final HubCredentials other = (HubCredentials) obj;
		if (actualPasswordLength != other.actualPasswordLength) {
			return false;
		}
		if (encryptedPassword == null) {
			if (other.encryptedPassword != null) {
				return false;
			}
		} else if (!encryptedPassword.equals(other.encryptedPassword)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

	public String getUsername() {
		return username;
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public int getActualPasswordLength() {
		return actualPasswordLength;
	}

}
