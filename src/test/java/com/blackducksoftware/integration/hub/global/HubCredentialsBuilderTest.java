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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubCredentialsBuilderTest {
	private static final String VALID_PASSWORD = "Password";
	private static final String VALID_USERNAME = "User";
	private static final String ERROR_MSG_NO_PASSWORD_FOUND = "No Hub Password was found.";
	private static final String ERROR_MSG_NO_USER_FOUND = "No Hub Username was found.";

	private List<String> expectedMessages;

	private TestLogger logger;

	@Before
	public void setUp() {
		expectedMessages = new ArrayList<String>();
		logger = new TestLogger();
	}

	@After
	public void tearDown() {
		final List<String> outputList = logger.getOutputList();
		final String outputString = logger.getOutputString();
		assertEquals("Too many/not enough messages expected: \n" + outputString, expectedMessages.size(),
				outputList.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage, outputList.contains(expectedMessage));
		}
	}

	@Test
	public void testvalidateCredentialsNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testvalidateCredentialsEmpty() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername("");
		builder.setPassword("   ");

		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testvalidateCredentials() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);

		assertTrue(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateHubUserNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validateUsername(logger));
	}

	@Test
	public void testValidateHubUser() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername(VALID_USERNAME);
		assertTrue(builder.validateUsername(logger));
	}

	@Test
	public void testValidateHubPasswordNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validatePassword(logger));
	}

	@Test
	public void testValidateHubPassword() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setPassword(VALID_PASSWORD);
		assertTrue(builder.validatePassword(logger));
	}

}
