package com.blackducksoftware.integration.hub.encryption;

import java.io.File;
import java.io.FileOutputStream;
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
import javax.crypto.KeyGenerator;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import com.blackducksoftware.integration.hub.exception.EncryptionException;

public final class PasswordEncrypter {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String EMBEDDED_SUN_KEY_FILE = "/Sun-Key.jceks";
	private static final String EMBEDDED_IBM_KEY_FILE = "/IBM-Key.jceks";

	private static final Logger logger = LoggerFactory.getLogger(PasswordEncrypter.class);

	private PasswordEncrypter() {
	}

	public static void main(final String[] args) {
		try {
			if (args == null || args.length == 0) {
				throw new IllegalArgumentException(
						"Please provide a UserName and Password, UserName should be provided first.");
			} else if (args.length == 1) {
				throw new IllegalArgumentException(
						"Please provide both a UserName and Password, UserName should be provided first.");
			} else if (args.length > 2) {
				throw new IllegalArgumentException(
						"Please ONLY provide a UserName and Password, UserName should be provided first.");
			}
			// TODO IMPROVEMENT: take a file path for a key file and a password
			// to get the Key from the KeyStore

			logMessage(args[0] + " = " + publicEncrypt(args[1]));
		} catch (final IllegalArgumentException e) {
			if (args != null) {
				logMessage("# of arguments = " + args.length);
			}
			logError("Example input: UserName Password", null);
			logError(e.getMessage(), e);
		} catch (final EncryptionException e) {
			logError(e.getMessage(), e);
		}
	}

	public static String publicEncrypt(final String password) throws IllegalArgumentException, EncryptionException {
		if (password == null || password.length() == 0) {
			throw new IllegalArgumentException("Please provide a UserName and Password.");
		}
		// TODO IMPROVEMENT: take a file path for a key file and a password to
		// get the Key from the KeyStore

		// needs to be at least 8 characters
		final char[] keyPass = { 'b', 'l', 'a', 'c', 'k', 'd', 'u', 'c', 'k', '1', '2', '3', 'I', 'n', 't', 'e', 'g',
				'r', 'a', 't', 'i', 'o', 'n' };
		Key key;
		key = getKey(null, keyPass);
		String encryptedPassword = null;
		if (key == null) {
			throw new EncryptionException("The encryption key is null");
		} else {
			encryptedPassword = encrypt(key, password);
		}
		if (encryptedPassword == null) {
			throw new EncryptionException("The encrypted Password is null");
		} else {
			return encryptedPassword;
		}
	}

	/**
	 * Encrypts the password provided. Returns the encrypted or version.
	 *
	 * @param keyToUse
	 *            Key to use for the encryption
	 * @param password
	 *            String to be encrypted
	 *
	 * @return String encrypted password.
	 */
	private static String encrypt(final Key keyToUse, final String password) {
		String reconstitutedString = null;
		try {
			byte[] buffer = null;
			byte[] bytes = null;
			final Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			bytes = password.getBytes(UTF8);
			// // org.apache.xml.security.Init.init();

			cipher.init(Cipher.ENCRYPT_MODE, keyToUse);
			bytes = Arrays.copyOf(bytes, 64);
			buffer = cipher.doFinal(bytes);
			buffer = Arrays.copyOf(buffer, 64);

			reconstitutedString = new String(Base64.encodeBase64(buffer), UTF8).trim();

		} catch (final Exception e) {
			logError(e.getMessage(), e);
		}

		return reconstitutedString;
	}

	/**
	 * Retrieves the cipher Key.
	 *
	 * @param instream
	 *            InputStream to get the Key from. If null it will use the
	 *            default cipher keys provided.
	 * @param keypass
	 *            char[] with the key password that will gain access to the key
	 *            (currently hard coded in)
	 * @return Key
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateException
	 */
	private static Key getKey(final InputStream is, final char[] keypass) {
		Key key = null;
		if (is == null) { // Get default cipher keys that are provided
			// attempts to retrieve the Sun cipher key, if that doesnt work then
			// it tries to get the Ibm key
			InputStream defaultIs = null;
			try {
				defaultIs = PasswordEncrypter.class.getResourceAsStream(EMBEDDED_SUN_KEY_FILE);
				try {
					final KeyStore keystore = KeyStore.getInstance("JCEKS");
					keystore.load(defaultIs, keypass);
					key = keystore.getKey("keyStore", keypass);
					return key;
				} finally {
					if (defaultIs != null) {
						defaultIs.close();

					}
				}
			} catch (final Exception e) {
				try {
					defaultIs = PasswordEncrypter.class.getResourceAsStream(EMBEDDED_IBM_KEY_FILE);
					try {
						final KeyStore keystore = KeyStore.getInstance("JCEKS");
						keystore.load(defaultIs, keypass);
						key = keystore.getKey("keyStore", keypass);
						return key;
					} finally {
						if (defaultIs != null) {
							defaultIs.close();

						}
					}
				} catch (final Exception e1) {
					logError("Failed to retrieve the encryption Key.", e);
				}
			}
		} // else {
			// TODO get the key at the specified Input Stream
			// }
		return null;
	}

	/**
	 * Generates a new key. Should be used manually and only when creating a new
	 * key is necessarry.
	 *
	 *
	 * @param keypass
	 *            char[] with the keypass that will gain access to the key
	 *            (currently hard coded in)
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static Key setKey(final char[] keypass, final File keyFile) {
		// If the current keys are replaced then we will not be able to decrypt
		// passwords that were encrypted with the
		// old keys
		Key key = null;
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(keyFile.getCanonicalPath());
			key = KeyGenerator.getInstance("DES").generateKey();
			final KeyStore keystore = KeyStore.getInstance("JCEKS");
			keystore.load(null, null);
			keystore.setKeyEntry("keyStore", key, keypass, null);
			keystore.store(output, keypass);
		} catch (final Exception e) {
			logError("Problem setting the encryption Key. ", e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (final IOException e) {
					logError("Problem closing the OutputStream for file at : " + keyFile.getPath(), e);
				}
			}
		}

		return key;
	}

	private static void logError(final String txt, final Throwable e) {
		final StringWriter sw = new StringWriter();
		if (e != null) {
			e.printStackTrace(new PrintWriter(sw));
		}
		if (logger != null && !(logger instanceof NOPLogger)) {
			logger.error(txt);
			if (e != null) {
				logger.error(sw.toString());
			}
		} else {
			// If no logger can be found print to System error
			System.err.println(txt);
			if (e != null) {
				System.err.println(sw.toString());
			}
		}
	}

	private static void logMessage(final String txt) {
		if (logger != null && !(logger instanceof NOPLogger)) {
			logger.info(txt);
		} else {
			// If no logger can be found print to System out
			System.out.println(txt);
		}
	}

}
