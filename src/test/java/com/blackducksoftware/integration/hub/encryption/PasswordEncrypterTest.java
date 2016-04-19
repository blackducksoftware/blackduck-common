package com.blackducksoftware.integration.hub.encryption;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;

public final class PasswordEncrypterTest {
	@Test
	public void testMainEncryptPassword() throws Exception {
		final TestLogger testLogger = new TestLogger();
		final String encryptedPassword = PasswordEncrypter.encrypt(testLogger, "Password");

		assertEquals("SaTaqurAqc7q0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4Q==",
				encryptedPassword);
	}

}
