package com.blackducksoftware.integration.hub.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.util.TestLogger;

public class PasswordDecrypterTest {
	private static Properties encryptedUserPassword = null;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void init() throws URISyntaxException, IOException {
		encryptedUserPassword = new Properties();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("encryptedPasswordFile.txt");
		try {
			encryptedUserPassword.load(is);
		} catch (final IOException e) {
			System.err.println("reading encryptedPasswordFile failed!");
		}
	}

	@Test
	public void testPasswordDecryption() throws Exception {
		final TestLogger testLogger = new TestLogger();
		assertEquals("super", PasswordDecrypter.decrypt(testLogger, encryptedUserPassword.getProperty("super")));
	}

	@Test
	public void testPasswordDecryptionAgain() throws Exception {
		final TestLogger testLogger = new TestLogger();
		assertEquals("testing",
				PasswordDecrypter.decrypt(testLogger, encryptedUserPassword.getProperty("test@blackducksoftware.com")));
	}

	@Test
	public void testPasswordDecryptionEmptyKey() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Please provide a non-blank password.");

		final TestLogger testLogger = new TestLogger();
		assertNull(PasswordDecrypter.decrypt(testLogger, ""));
	}

	@Test
	public void testPasswordDecryptionNullKey() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Please provide a non-blank password.");

		final TestLogger testLogger = new TestLogger();
		assertNull(PasswordDecrypter.decrypt(testLogger, null));
	}

}
