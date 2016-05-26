/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResult;
import com.blackducksoftware.integration.hub.builder.ValidationResults;

public class HubServerConfigBuilderTest {

	private List<String> expectedMessages;
	private List<String> actualMessages;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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

	private List<String> getMessages(final ValidationResults<GlobalFieldKey, HubServerConfig> result) {

		final List<String> messageList = new ArrayList<String>();
		final Map<GlobalFieldKey, List<ValidationResult>> resultMap = result.getResultMap();
		for (final GlobalFieldKey key : resultMap.keySet()) {
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
	public void testEmptyConfigValidationsWithDefaults() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();

		builder.validateHubUrl(result);
		assertFalse(result.isSuccess());
		builder.validateTimeout(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testEmptyConfigValidationsWithoutDefaults() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();

		builder.validateHubUrl(result);
		assertFalse(result.isSuccess());
		builder.validateTimeout(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUrlNull() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl(null);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUrlEmpty() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUrlInvalid() throws Exception {
		expectedMessages.add("The Hub Url is not a valid URL.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("ThisIsNotAUrl");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUrlValid() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);

		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateHubTimeoutNullWithDefaults() throws Exception {
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNull() throws Exception {
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutEmptyWithDeafults() throws Exception {
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout("  ");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutEmpty() throws Exception {
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout("  ");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNotIntegerWithDeafults() throws Exception {
		expectedMessages.add("The String : Not Integer , is not an Integer.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout("Not Integer");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNotInteger() throws Exception {
		expectedMessages.add("The String : Not Integer , is not an Integer.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout("Not Integer");

		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNegativeWithDefaults() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout(-1200);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNegative() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout(-1200);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeout() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout(1200);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateHubTimeoutStringWithDefaults() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout("120");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertTrue(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutString() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout("120");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertTrue(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testEmptyConfigIsInvalidWithDefaults() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = builder.build();
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testEmptyConfigIsInvalid() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");
		expectedMessages.add("No Hub Timeout was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = builder.build();
		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testBuildConfigInvalidWithDefaults() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setHubUrl("https://google.com");
		builder.setTimeout("-122134");
		builder.setUsername("User");
		builder.setPassword("Pass");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = builder.build();
		assertFalse(result.hasErrors());

		final HubServerConfig config = result.getConstructedObject();

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals(HubServerConfigBuilder.DEFAULT_TIMEOUT, config.getTimeout());
		assertEquals("User", config.getGlobalCredentials().getUsername());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
	}

	@Test
	public void testBuildConfigInvalid() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setTimeout("-122134");
		builder.setUsername("User");
		builder.setPassword("Pass");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = builder.build();
		assertTrue(result.hasErrors());

		final HubServerConfig config = result.getConstructedObject();

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals(-122134, config.getTimeout());
		assertEquals("User", config.getGlobalCredentials().getUsername());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidConfig() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaults(builder);
		final HubServerConfig config = builder.build().getConstructedObject();

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getUsername());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
	}


	private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
		builder.setHubUrl("https://google.com");
		builder.setTimeout("100");
		builder.setUsername("User");
		builder.setPassword("Pass");
	}

}
