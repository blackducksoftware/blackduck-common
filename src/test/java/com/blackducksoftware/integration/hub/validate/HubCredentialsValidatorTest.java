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
