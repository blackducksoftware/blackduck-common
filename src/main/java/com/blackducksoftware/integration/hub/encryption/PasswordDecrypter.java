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
