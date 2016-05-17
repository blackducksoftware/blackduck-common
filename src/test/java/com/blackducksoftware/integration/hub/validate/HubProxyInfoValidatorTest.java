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
