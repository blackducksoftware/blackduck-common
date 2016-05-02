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
package com.blackducksoftware.integration.hub.validate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestProxyInfoValidator;

public class HubProxyInfoValidatorTest {
	private static final String TEST_PASSWORD = "testPassword";
	private static final String TEST_USER = "testUser";
	private static final String TEST_HOST = "testHost";

	private TestProxyInfoValidator validator;

	@Before
	public void init() {
		validator = new TestProxyInfoValidator();
	}

	@Test
	public void testInvalidPort() throws Exception {
		assertFalse(validator.validatePort(TEST_HOST, -1));
	}

	@Test
	public void testValidPort() throws Exception {
		assertTrue(validator.validatePort(TEST_HOST, 123456));
		assertTrue(validator.validatePort(TEST_HOST, "123456"));
		assertTrue(validator.validatePort(TEST_HOST, 0));
		assertTrue(validator.validatePort(TEST_HOST, ""));
		assertTrue(validator.validatePort(TEST_HOST, null));
	}

	@Test
	public void testValidCredentials() throws Exception {
		assertTrue(validator.validateCredentials(TEST_HOST, TEST_USER, TEST_PASSWORD));
		assertTrue(validator.validateCredentials(TEST_HOST, "", ""));
	}

	@Test
	public void testInvalidCredentials() throws Exception {
		assertFalse(validator.validateCredentials(TEST_HOST, TEST_USER, ""));
		assertFalse(validator.validateCredentials(TEST_HOST, "", TEST_PASSWORD));
		assertFalse(validator.validateCredentials("", TEST_USER, ""));
		assertFalse(validator.validateCredentials("", "", TEST_PASSWORD));
		assertFalse(validator.validateCredentials(TEST_HOST, TEST_USER, null));
		assertFalse(validator.validateCredentials(TEST_HOST, null, TEST_PASSWORD));
		assertFalse(validator.validateCredentials("", TEST_USER, null));
		assertFalse(validator.validateCredentials("", null, TEST_PASSWORD));
	}

	@Test
	public void testValidIgnoreHosts() throws Exception {
		final String ignoreHost = "google";
		final String ignoreHostList = "google,[a-zA-Z]*,yahoo,msn";

		assertTrue(validator.validateIgnoreHosts(TEST_HOST, ignoreHost));
		assertTrue(validator.validateIgnoreHosts(TEST_HOST, ignoreHostList));
		assertTrue(validator.validateIgnoreHosts(TEST_HOST, ignoreHostList));
	}

	@Test
	public void testInvalidIgnoreHosts() throws Exception {
		final String ignoreHost = "[^3-?";
		final String ignoreHostList = "google,[^3-?,yahoo,msn";

		assertFalse(validator.validateIgnoreHosts(TEST_HOST, ignoreHost));
		assertFalse(validator.validateIgnoreHosts(TEST_HOST, ignoreHostList));
		assertFalse(validator.validateIgnoreHosts("", ignoreHost));
		assertFalse(validator.validateIgnoreHosts(TEST_HOST, ignoreHostList));
	}
}
