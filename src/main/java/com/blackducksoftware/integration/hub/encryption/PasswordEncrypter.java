package com.blackducksoftware.integration.hub.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.exception.EncryptionException;

public final class PasswordEncrypter {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String EMBEDDED_SUN_KEY_FILE = "/Sun-Key.jceks";
	private static final String EMBEDDED_IBM_KEY_FILE = "/IBM-Key.jceks";

	// needs to be at least 8 characters
	private static final char[] KEY_PASS = { 'b', 'l', 'a', 'c', 'k', 'd', 'u', 'c', 'k', '1', '2', '3', 'I', 'n', 't',
			'e', 'g', 'r', 'a', 't', 'i', 'o', 'n' };

	private static final Logger logger = LoggerFactory.getLogger(PasswordEncrypter.class);

	private PasswordEncrypter() {
	}

	public static String encrypt(final String password) throws IllegalArgumentException, EncryptionException {
		if (StringUtils.isBlank(password)) {
			throw new IllegalArgumentException("Please provide a non-blank password.");
		}

		final Key key = getKey();

		final String encryptedPassword = encrypt(key, password);
		return encryptedPassword;
	}

	private static String encrypt(final Key key, final String password) throws EncryptionException {
		String reconstitutedString = null;
		try {
			byte[] buffer = null;
			byte[] bytes = null;
			final Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			bytes = password.getBytes(UTF8);

			cipher.init(Cipher.ENCRYPT_MODE, key);
			bytes = Arrays.copyOf(bytes, 64);
			buffer = cipher.doFinal(bytes);
			buffer = Arrays.copyOf(buffer, 64);

			reconstitutedString = new String(Base64.encodeBase64(buffer), UTF8).trim();
		} catch (final Exception e) {
			logError(e.getMessage(), e);
		}

		if (null == reconstitutedString) {
			throw new EncryptionException("The encrypted password is null");
		}

		return reconstitutedString;
	}

	private static Key getKey() throws EncryptionException {
		Key key = null;
		try {
			key = retrieveKeyFromFile(EMBEDDED_SUN_KEY_FILE);
		} catch (final Exception e) {
			try {
				key = retrieveKeyFromFile(EMBEDDED_IBM_KEY_FILE);
			} catch (final Exception e1) {
				logError("Failed to retrieve the encryption Key.", e);
			}
		}

		if (null == key) {
			throw new EncryptionException("The encryption key is null");
		}

		return key;
	}

	private static Key retrieveKeyFromFile(final String keyFile) throws NoSuchAlgorithmException, CertificateException,
			IOException, UnrecoverableKeyException, KeyStoreException {
		final InputStream inputStream = PasswordEncrypter.class.getResourceAsStream(keyFile);
		try {
			final KeyStore keystore = KeyStore.getInstance("JCEKS");
			keystore.load(inputStream, KEY_PASS);
			final Key key = keystore.getKey("keyStore", KEY_PASS);
			return key;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private static void logError(final String errorMessage, final Exception e) {
		final StringWriter sw = new StringWriter();
		if (e != null) {
			e.printStackTrace(new PrintWriter(sw));
		}

		logger.error(errorMessage);
		logger.error(sw.toString());
	}

}
