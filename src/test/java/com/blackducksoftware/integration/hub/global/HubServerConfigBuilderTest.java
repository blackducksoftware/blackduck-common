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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResult;
import com.blackducksoftware.integration.hub.builder.ValidationResults;

public class HubServerConfigBuilderTest {

	private static Properties testProperties;

	private List<String> expectedMessages;
	private List<String> actualMessages;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void testInit() throws Exception {
		testProperties = new Properties();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("test.properties");
		try {
			testProperties.load(is);
		} catch (final IOException e) {
			System.err.println("reading test.properties failed!");
		}
	}

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
	public void testEmptyConfigValidations() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
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
	public void testValidateHubUrlValidThroughInvalidProxy() throws Exception {
		expectedMessages.add("Can not reach this server : https://google.com caused by : FakeHost");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setProxyHost("FakeHost");
		builder.setProxyPort(3128);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubUrlValidThroughPassThroughProxy() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaults(builder);
		builder.setHubUrl("https://google.com");
		setBuilderProxyDefaults(builder);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateHubUrl(result);

		assertTrue(result.isSuccess());
	}

	@Test
	public void testValidateHubTimeoutNull() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutEmpty() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		builder.setTimeout("  ");
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testValidateHubTimeoutNotInteger() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("The String : Not Integer , is not an Integer.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout("Not Integer");
	}

	@Test
	public void testValidateHubTimeoutNegative() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
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
	public void testValidateHubTimeoutString() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = new ValidationResults<GlobalFieldKey, HubServerConfig>();
		builder.validateTimeout(result);

		assertFalse(result.isSuccess());

		actualMessages = getMessages(result);
	}

	@Test
	public void testEmptyConfigIsInvalid() throws Exception {
		expectedMessages.add("No Hub Url was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = builder.build();
		assertFalse(result.isSuccess());

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

	@Test
	public void testValidConfigWithProxies() throws Exception {

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaults(builder);
		setBuilderProxyDefaults(builder);
		final HubServerConfig config = builder.build().getConstructedObject();

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getUsername());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
		assertEquals(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
		assertEquals(NumberUtils.toInt(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
				config.getProxyInfo().getPort());
	}

	@Test
	public void testValidConfigWithProxiesNoProxy() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaults(builder);
		setBuilderProxyDefaults(builder);
		builder.setIgnoredProxyHosts("google");
		final HubServerConfig config = builder.build().getConstructedObject();

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getUsername());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
		assertEquals(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
		assertEquals(NumberUtils.toInt(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
				config.getProxyInfo().getPort());
		assertEquals("google", config.getProxyInfo().getIgnoredProxyHosts());
		assertFalse(config.getProxyInfo().shouldUseProxyForUrl(config.getHubUrl()));
	}

	private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
		builder.setHubUrl("https://google.com");
		builder.setTimeout("100");
		builder.setUsername("User");
		builder.setPassword("Pass");
	}

	private void setBuilderProxyDefaults(final HubServerConfigBuilder builder) throws Exception {
		builder.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
		builder.setProxyPort(NumberUtils.toInt(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
	}

}
