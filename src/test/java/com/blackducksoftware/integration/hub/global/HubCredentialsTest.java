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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.util.TestLogger;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class HubCredentialsTest {
	@Test
	public void testHubCredentials() throws IllegalArgumentException, EncryptionException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, HubIntegrationException {
		final TestLogger logger = new TestLogger();

		final String hubUser1 = "hubUser1";
		final String hubPass1 = "hubPass1";

		final String hubUser2 = "hubUser2";
		final String hubPass2Clear = "hubPass2";
		final String hubPass2 = PasswordEncrypter.encrypt(hubPass2Clear);

		HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername(hubUser1);
		builder.setPassword(hubPass1);
		final HubCredentials item1 = builder.build(logger);

		builder = new HubCredentialsBuilder();
		builder.setUsername(hubUser2);
		builder.setPassword(hubPass2Clear);
		final HubCredentials item2 = builder.build(logger);

		builder = new HubCredentialsBuilder();
		builder.setUsername(hubUser1);
		builder.setPassword(hubPass1);
		final HubCredentials item3 = builder.build(logger);

		assertEquals(hubUser1, item1.getUsername());
		assertEquals(hubPass1, item1.getDecryptedPassword());
		assertEquals("********", item1.getMaskedPassword());

		assertEquals(hubUser2, item2.getUsername());
		assertEquals(hubPass2, item2.getEncryptedPassword());

		assertEquals(hubPass2Clear, item2.getDecryptedPassword());
		assertEquals("********", item2.getMaskedPassword());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(HubCredentials.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final String expected = "HubCredentials [username=" + item1.getUsername() + ", encryptedPassword="
				+ item1.getEncryptedPassword() + ", actualPasswordLength=" + item1.getActualPasswordLength() + "]";
		assertEquals(expected, item1.toString());
	}

}
