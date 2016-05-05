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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResult;
import com.blackducksoftware.integration.hub.builder.ValidationResults;

public class HubCredentialsBuilderTest {
	private static final String VALID_PASSWORD = "Password";
	private static final String VALID_USERNAME = "User";
	private static final String ERROR_MSG_NO_PASSWORD_FOUND = "No Hub Password was found.";
	private static final String ERROR_MSG_NO_USER_FOUND = "No Hub Username was found.";

	private List<String> expectedMessages;
	private List<String> actualMessages;

	@Before
	public void setUp() {
		expectedMessages = new ArrayList<String>();
		actualMessages = new ArrayList<String>();
	}

	@After
	public void tearDown() {
		assertEquals("Too many/not enough messages expected: \n" + actualMessages.size(), expectedMessages.size(),
				actualMessages.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage,
					actualMessages.contains(expectedMessage));
		}
	}

	private List<String> getMessages(final ValidationResults<String, HubCredentials> result) {

		final List<String> messageList = new ArrayList<String>();
		final Map<String, List<ValidationResult>> resultMap = result.getResultMap();
		for (final String key : resultMap.keySet()) {
			final List<ValidationResult> resultList = resultMap.get(key);

			for (final ValidationResult item : resultList) {
				final String message = item.getMessage();

				if (StringUtils.isNotBlank(message)) {
					messageList.add(item.getMessage());
				}
			}
		}
		return messageList;
	}

	@Test
	public void testvalidateCredentialsNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validateCredentials(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testvalidateCredentialsEmpty() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername("");
		builder.setPassword("   ");
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validateCredentials(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testvalidateCredentials() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validateCredentials(result);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateHubUserNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_USER_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validateUsername(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUser() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername(VALID_USERNAME);
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validateUsername(result);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateHubPasswordNull() throws Exception {
		expectedMessages.add(ERROR_MSG_NO_PASSWORD_FOUND);

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validatePassword(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubPassword() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setPassword(VALID_PASSWORD);
		final ValidationResults<String, HubCredentials> result = new ValidationResults<String, HubCredentials>();
		builder.validatePassword(result);
		assertTrue(result.isSuccess());
	}
}
