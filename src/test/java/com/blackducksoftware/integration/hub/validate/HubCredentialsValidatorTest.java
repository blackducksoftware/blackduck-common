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

import com.blackducksoftware.integration.hub.util.TestCredentialsValidator;

public class HubCredentialsValidatorTest {

	private static final String HUB_USER = "hubUser";
	private static final String HUB_PASS = "hubPass";
	private TestCredentialsValidator validator;

	@Before
	public void init() {
		validator = new TestCredentialsValidator();
	}

	@Test
	public void testValidateCredentials() throws Exception {

		final String hubUser = HUB_USER;
		final String hubPass = HUB_PASS;

		assertTrue(validator.validateCredentials(hubUser, hubPass));
		assertFalse(validator.validateCredentials("", hubPass));
		assertFalse(validator.validateCredentials(hubUser, ""));
		assertFalse(validator.validateCredentials("", ""));
		assertFalse(validator.validateCredentials(null, hubPass));
		assertFalse(validator.validateCredentials(hubUser, null));
		assertFalse(validator.validateCredentials(null, null));
	}

	@Test
	public void testInvalidUserName() throws Exception {
		assertFalse(validator.validateUserName(""));
		assertFalse(validator.validateUserName(null));
	}

	@Test
	public void testValidUserName() throws Exception {
		assertTrue(validator.validateUserName(HUB_USER));
	}

	@Test
	public void testInvalidPassword() throws Exception {
		assertFalse(validator.validatePassword(""));
		assertFalse(validator.validatePassword(null));
	}

	@Test
	public void testValidPassword() throws Exception {
		assertTrue(validator.validatePassword(HUB_PASS));
	}
}
