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

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubCredentials implements Serializable {

	private static final long serialVersionUID = 7924589951692259151L;

	private final String hubUser;

	private final String hubPass;

	private final Integer actualPasswordLength;

	/**
	 * The password should be encrypted when passed into this constructor.
	 *
	 */
	public HubCredentials(final String hubUser, final String encryptedPassword, final Integer actualPasswordLength) {
		this.hubUser = hubUser;
		hubPass = encryptedPassword;
		this.actualPasswordLength = actualPasswordLength;
	}

	public String getHubUser() {
		return hubUser;
	}

	public String getEncryptedPassword() {
		return hubPass;
	}

	public String getMaskedPassword()
			throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
			EncryptionException {
		if (StringUtils.isBlank(hubPass)) {
			return null;
		}
		if (actualPasswordLength != null) {
			final char[] array = new char[actualPasswordLength];
			Arrays.fill(array, '*');
			return new String(array);
		} else {
			return null;
		}
	}

	public String getDecryptedPassword(final IntLogger logger)
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			EncryptionException {
		if (StringUtils.isBlank(hubPass)) {
			return null;
		}
		return PasswordDecrypter.decrypt(logger, hubPass);
	}

	public boolean isEmpty() {
		return StringUtils.isBlank(hubUser) && StringUtils.isBlank(hubPass);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubCredentials [hubUser=");
		builder.append(hubUser);
		builder.append(", hubPass=");
		builder.append(hubPass);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actualPasswordLength == null) ? 0 : actualPasswordLength.hashCode());
		result = prime * result + ((hubPass == null) ? 0 : hubPass.hashCode());
		result = prime * result + ((hubUser == null) ? 0 : hubUser.hashCode());
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
		if (actualPasswordLength == null) {
			if (other.actualPasswordLength != null) {
				return false;
			}
		} else if (!actualPasswordLength.equals(other.actualPasswordLength)) {
			return false;
		}
		if (hubPass == null) {
			if (other.hubPass != null) {
				return false;
			}
		} else if (!hubPass.equals(other.hubPass)) {
			return false;
		}
		if (hubUser == null) {
			if (other.hubUser != null) {
				return false;
			}
		} else if (!hubUser.equals(other.hubUser)) {
			return false;
		}
		return true;
	}


}
