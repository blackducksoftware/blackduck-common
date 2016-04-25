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
		assertEquals("super", PasswordDecrypter.decrypt(encryptedUserPassword.getProperty("super")));
	}

	@Test
	public void testPasswordDecryptionAgain() throws Exception {
		assertEquals("testing",
				PasswordDecrypter.decrypt(encryptedUserPassword.getProperty("test@blackducksoftware.com")));
	}

	@Test
	public void testPasswordDecryptionEmptyKey() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Please provide a non-blank password.");

		assertNull(PasswordDecrypter.decrypt(""));
	}

	@Test
	public void testPasswordDecryptionNullKey() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Please provide a non-blank password.");

		assertNull(PasswordDecrypter.decrypt(null));
	}

}
