package com.blackducksoftware.integration.hub.encryption;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PasswordEncrypterTest {
	@Test
	public void testMainEncryptPassword() throws Exception {
		final String encryptedPassword = PasswordEncrypter.encrypt("Password");

		assertEquals("SaTaqurAqc7q0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4erSd/Sfogvh6tJ39J+iC+Hq0nf0n6IL4Q==",
				encryptedPassword);
	}

}
