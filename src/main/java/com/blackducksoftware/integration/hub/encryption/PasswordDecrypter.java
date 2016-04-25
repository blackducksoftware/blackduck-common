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
package com.blackducksoftware.integration.hub.encryption;

import javax.crypto.Cipher;

import com.blackducksoftware.integration.hub.exception.EncryptionException;

public final class PasswordDecrypter {
	private PasswordDecrypter() {
	}

	public static String decrypt(final String password) throws IllegalArgumentException, EncryptionException {
		final EncryptionUtils encryptionUtils = new EncryptionUtils();

		final String decryptedPassword = encryptionUtils.alterString(password, null, Cipher.DECRYPT_MODE);

		if (null == decryptedPassword) {
			throw new EncryptionException("The decrypted password is null");
		}

		return decryptedPassword;
	}

}
