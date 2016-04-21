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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubServerConfigBuilderTest {

	private static Properties testProperties;

	private List<String> expectedMessages;

	private TestLogger logger;

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
		logger = new TestLogger();
	}

	@After
	public void tearDown() {
		final List<String> outputList = logger.getOutputList();
		final String outputString = logger.getOutputString();
		assertEquals("Too many/not enough messages expected: \n" + outputString, expectedMessages.size(), outputList.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage, outputList.contains(expectedMessage));
		}
	}

	@Test
	public void testEmptyConfigValidations() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("The Timeout must be greater than 0.");
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		assertTrue(builder.validateProxyConfig(logger));
		assertTrue(!builder.validateHubUrl(logger));
		assertTrue(!builder.validateHubCredentials(logger));
		assertTrue(!builder.validateTimeout(logger));
	}

	@Test
	public void testValidateProxyConfigHubUrlIgnored() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setIgnoredProxyHosts("google");

		builder.validateProxyConfig(logger);
		assertTrue(builder.isHubUrlIgnored());
	}

	@Test
	public void testValidateProxyConfigHubUrlNotIgnored() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setIgnoredProxyHosts("test");

		builder.validateProxyConfig(logger);
		assertTrue(!builder.isHubUrlIgnored());
	}

	@Test
	public void testValidateHubUrlNull() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl(null);
		assertTrue(!builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubUrlEmpty() throws Exception {
		expectedMessages.add("No Hub Url was found.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("");
		assertTrue(!builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubUrlInvalid() throws Exception {
		expectedMessages.add("The Hub Url is not a valid URL.");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("ThisIsNotAUrl");
		assertTrue(!builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubUrlValid() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		assertTrue(builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubUrlValidThroughInvalidProxy() throws Exception {
		expectedMessages.add("Can not reach this server : https://google.com");
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setProxyHost("FakeHost");
		builder.setProxyPort(3128);
		assertTrue(!builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubUrlValidThroughPassThroughProxy() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl("https://google.com");
		builder.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
		builder.setProxyPort(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));

		assertTrue(builder.validateHubUrl(logger));
	}

	@Test
	public void testValidateHubCredentialsNull() throws Exception {
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		assertTrue(!builder.validateHubCredentials(logger));
	}

	@Test
	public void testValidateHubCredentialsEmpty() throws Exception {
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUser("");
		builder.setHubPass("   ");

		assertTrue(!builder.validateHubCredentials(logger));
	}

	@Test
	public void testValidateHubCredentials() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUser("User");
		builder.setHubPass("Password");

		assertTrue(builder.validateHubCredentials(logger));
	}

	@Test
	public void testValidateHubUserNull() throws Exception {
		expectedMessages.add("No Hub Username was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		assertTrue(!builder.validateHubUser(logger));
	}

	@Test
	public void testValidateHubUser() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUser("User");
		assertTrue(builder.validateHubUser(logger));
	}

	@Test
	public void testValidateHubPasswordNull() throws Exception {
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		assertTrue(!builder.validateHubPassword(logger));
	}

	@Test
	public void testValidateHubPassword() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubPass("Password");
		assertTrue(builder.validateHubPassword(logger));
	}

	@Test
	public void testValidateHubTimeoutNull() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		assertTrue(!builder.validateTimeout(logger));
	}

	@Test
	public void testValidateHubTimeoutEmpty() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout("  ");

		assertTrue(!builder.validateTimeout(logger));
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

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout(-1200);
		assertTrue(!builder.validateTimeout(logger));
	}

	@Test
	public void testValidateHubTimeout() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setTimeout(1200);
		assertTrue(builder.validateTimeout(logger));
	}

	@Test
	public void testValidateHubTimeoutString() throws Exception {
		expectedMessages.add("The Timeout must be greater than 0.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();

		assertTrue(!builder.validateTimeout(logger));
	}


	@Test
	public void testEmptyConfigIsInvalid() throws Exception {
		thrown.expect(HubIntegrationException.class);
		thrown.expectMessage("The server configuration is not valid - please check the log for the specific issues.");
		expectedMessages.add("No Hub Url was found.");
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.build(logger);
	}

	@Test
	public void testValidConfig() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaults(builder);
		final HubServerConfig config = builder.build(logger);

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getHubUser());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
	}

	@Test
	public void testValidConfigWithProxies() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaultsWithProxies(builder);
		final HubServerConfig config = builder.build(logger);

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getHubUser());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
		assertEquals(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
		assertEquals(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
				config.getProxyInfo().getPort());
	}

	@Test
	public void testValidConfigWithProxiesNoProxy() throws Exception {
		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		setBuilderDefaultsWithProxies(builder);
		builder.setIgnoredProxyHosts("google");
		final HubServerConfig config = builder.build(logger);

		assertEquals(new URL("https://google.com"), config.getHubUrl());
		assertEquals("User", config.getGlobalCredentials().getHubUser());
		assertEquals("Pass", config.getGlobalCredentials().getDecryptedPassword());
		assertEquals(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), config.getProxyInfo().getHost());
		assertEquals(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")),
				config.getProxyInfo().getPort());
		assertEquals("google", config.getProxyInfo().getIgnoredProxyHosts());
		assertTrue(config.getProxyInfo().isHubUrlIgnored());
	}

	private void setBuilderDefaults(final HubServerConfigBuilder builder) throws Exception {
		builder.setHubUrl("https://google.com");
		builder.setHubUser("User");
		builder.setHubPass("Pass");
		builder.setTimeout("100");
	}

	private void setBuilderDefaultsWithProxies(final HubServerConfigBuilder builder) throws Exception {
		setBuilderDefaults(builder);

		builder.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
		builder.setProxyPort(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
	}

}
