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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.builder.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.util.TestLogger;
import com.blackducksoftware.integration.hub.util.TestServerConfigValidator;

public class HubServerConfigValidatorTest {

	private static Properties testProperties;
	private TestLogger logger;
	private TestServerConfigValidator validator;

	@Before
	public void init() {
		validator = new TestServerConfigValidator();
		logger = new TestLogger();

		testProperties = new Properties();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("test.properties");
		try {
			testProperties.load(is);
		} catch (final IOException e) {
			System.err.println("reading test.properties failed!");
		}
	}

	@Test
	public void testValidServerUrl() throws Exception {
		assertTrue(validator.validateServerUrl("https://google.com"));
	}

	@Test
	public void testValidServerUrlWithProxy() throws Exception {

		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
		proxyBuilder.setHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
		proxyBuilder.setPort(NumberUtils.toInt(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
		final HubProxyInfo proxyInfo = proxyBuilder.build(logger);
		assertTrue(validator.validateServerUrl("https://google.com", proxyInfo));
	}

	@Test
	public void testInvalidServerUrl() throws Exception {
		assertFalse(validator.validateServerUrl(null));
		assertFalse(validator.validateServerUrl(""));
		assertFalse(validator.validateServerUrl("NotaURL"));
	}

	@Test
	public void testInvalidServerUrlWithProxy() throws Exception {
		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
		proxyBuilder.setHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
		proxyBuilder.setPort(NumberUtils.toInt(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
		final HubProxyInfo proxyInfo = proxyBuilder.build(logger);
		assertFalse(validator.validateServerUrl(null, proxyInfo));
		assertFalse(validator.validateServerUrl("", proxyInfo));
		assertFalse(validator.validateServerUrl("NotaURL", proxyInfo));
	}

	@Test
	public void testValidServerUrlWithInvalidProxy() throws Exception {
		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
		proxyBuilder.setHost("FakeHost");
		proxyBuilder.setPort(3128);
		final HubProxyInfo proxyInfo = proxyBuilder.build(logger);
		assertFalse(validator.validateServerUrl("https://google.com", proxyInfo));
	}

	@Test
	public void testInvalidServerUrlWithInvalidProxy() throws Exception {
		final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
		proxyBuilder.setHost("FakeHost");
		proxyBuilder.setPort(3128);
		final HubProxyInfo proxyInfo = proxyBuilder.build(logger);
		assertFalse(validator.validateServerUrl("NotaURL", proxyInfo));
	}

	@Test
	public void testValidTimeout() throws Exception {
		assertTrue(validator.validateTimeout("10"));
		assertTrue(validator.validateTimeout("2"));
	}

	@Test
	public void testInvalidTimeout() throws Exception {
		assertFalse(validator.validateTimeout("-1"));
		assertFalse(validator.validateTimeout(null));
		assertFalse(validator.validateTimeout("0"));
		assertFalse(validator.validateTimeout("abcdz123"));
	}
}
