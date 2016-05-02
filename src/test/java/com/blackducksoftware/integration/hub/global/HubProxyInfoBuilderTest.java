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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubProxyInfoBuilderTest {
	private static final int VALID_PORT = 2303;
	private static final String VALID_HOST = "just need a non-empty string";
	private static final String VALID_PASSWORD = "itsasecret";
	private static final String VALID_USERNAME = "memyselfandi";
	private static final String VALID_IGNORE_HOST_LIST = "google,msn,yahoo";
	private static final String VALID_IGNORE_HOST = "google";
	private static final String INVALID_IGNORE_HOST_LIST = "google,[^-z!,abc";
	private static final String INVALID_IGNORE_HOST = "[^-z!";

	private List<String> expectedMessages;
	private TestLogger logger;

	@Rule
	public ExpectedException exception = ExpectedException.none();

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
	public void testValidateProxyConfigHubUrlIgnored() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST);

		final HubProxyInfo proxyInfo = builder.build(logger);
		final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));
		assertFalse(useProxy);
	}

	@Test
	public void testValidateProxyConfigHubUrlNotIgnored() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setIgnoredProxyHosts("test");

		final HubProxyInfo proxyInfo = builder.build(logger);
		final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));
		assertTrue(useProxy);
	}

	@Test
	public void testValidateProxyPort() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		assertTrue(builder.validatePort(logger));
	}

	@Test
	public void testValidateProxyPortNoHost() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost("");
		builder.setPort(VALID_PORT);
		assertTrue(builder.validatePort(logger));
	}

	@Test
	public void testValidateCredentialsNoHost() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost("");
		assertTrue(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateCredentialsBothEmpty() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setUsername("");
		builder.setPassword("");
		assertTrue(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateCredentialsBothNotEmpty() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		assertTrue(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateCredentialsUserOnly() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_CREDENTIALS_INVALID);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword("");
		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateCredentialsPasswordOnly() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_CREDENTIALS_INVALID);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setUsername("");
		builder.setPassword(VALID_PASSWORD);
		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateIgnoreHostNoProxyHost() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost("");
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST);
		assertTrue(builder.validateIgnoreHosts(logger));
	}

	@Test
	public void testValidateIgnoreHost() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST);
		assertTrue(builder.validateIgnoreHosts(logger));
	}

	@Test
	public void testValidateIgnoreHostList() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
		assertTrue(builder.validateIgnoreHosts(logger));
	}

	@Test
	public void testValidateIgnoreHostBadPattern() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_IGNORE_HOSTS_INVALID);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setIgnoredProxyHosts(INVALID_IGNORE_HOST);
		assertFalse(builder.validateIgnoreHosts(logger));
	}

	@Test
	public void testValidateIgnoreHostListBadPattern() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_IGNORE_HOSTS_INVALID);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setIgnoredProxyHosts(INVALID_IGNORE_HOST_LIST);
		assertFalse(builder.validateIgnoreHosts(logger));
	}

	@Test
	public void testAssertWithNoHost() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost("");
		builder.setPort(VALID_PORT);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithInvalidPort() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_PROXY_PORT_INVALID);
		exception.expect(HubIntegrationException.class);
		exception.expectMessage(HubProxyInfoBuilder.MSG_PROXY_INVALID_CONFIG);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(0);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithInvalidUser() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_CREDENTIALS_INVALID);
		exception.expect(HubIntegrationException.class);
		exception.expectMessage(HubProxyInfoBuilder.MSG_PROXY_INVALID_CONFIG);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername("");
		builder.setPassword(VALID_PASSWORD);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithInvalidPassword() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_CREDENTIALS_INVALID);
		exception.expect(HubIntegrationException.class);
		exception.expectMessage(HubProxyInfoBuilder.MSG_PROXY_INVALID_CONFIG);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword("");
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithInvalidIgnoreHost() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_IGNORE_HOSTS_INVALID);
		exception.expect(HubIntegrationException.class);
		exception.expectMessage(HubProxyInfoBuilder.MSG_PROXY_INVALID_CONFIG);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		builder.setIgnoredProxyHosts(INVALID_IGNORE_HOST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithInvalidIgnoreHostList() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.ERROR_MSG_IGNORE_HOSTS_INVALID);
		exception.expect(HubIntegrationException.class);
		exception.expectMessage(HubProxyInfoBuilder.MSG_PROXY_INVALID_CONFIG);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		builder.setIgnoredProxyHosts(INVALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testAssertWithValidInput() throws Exception {

		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);

		builder.assertValid(logger);
	}

	@Test
	public void testBuildWithValidInput() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setHost(VALID_HOST);
		builder.setPort(VALID_PORT);
		builder.setUsername(VALID_USERNAME);
		builder.setPassword(VALID_PASSWORD);
		builder.setIgnoredProxyHosts(VALID_IGNORE_HOST_LIST);
		final HubProxyInfo proxyInfo = builder.build(logger);
		assertNotNull(proxyInfo);
	}

	@Test
	public void testBuildWithEmptyInput() throws Exception {
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		expectedMessages.add(HubProxyInfoBuilder.WARN_MSG_PROXY_HOST_NOT_SPECIFIED);
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		final HubProxyInfo proxyInfo = builder.build(logger);
		assertNotNull(proxyInfo);
	}
}
