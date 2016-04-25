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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;

public class HubCredentials implements Serializable {
	private static final long serialVersionUID = 7924589951692259151L;

	private final String username;
	private final String encryptedPassword;
	private final int actualPasswordLength;

	public HubCredentials(final String username, final String password)
			throws IllegalArgumentException, EncryptionException {
		this.username = username;
		this.actualPasswordLength = password.length();
		this.encryptedPassword = PasswordEncrypter.encrypt(password);
	}

	public String getMaskedPassword() throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, EncryptionException {
		final char[] array = new char[actualPasswordLength];
		Arrays.fill(array, '*');
		return new String(array);
	}

	public String getDecryptedPassword() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, EncryptionException {
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
