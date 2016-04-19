package com.blackducksoftware.integration.hub.encryption;

import javax.crypto.Cipher;

import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public final class PasswordEncrypter {
	private PasswordEncrypter() {
	}

	public static String encrypt(final IntLogger logger, final String password)
			throws IllegalArgumentException, EncryptionException {
		final EncryptionUtils encryptionUtils = new EncryptionUtils();

		final String encryptedPassword = encryptionUtils.alterString(logger, password, null, Cipher.ENCRYPT_MODE);

		if (null == encryptedPassword) {
			throw new EncryptionException("The encrypted password is null");
		}

		return encryptedPassword;
	}

}
