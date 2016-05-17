/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

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
