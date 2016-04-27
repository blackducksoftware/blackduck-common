package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

public class HubProxyInfoBuilderTest {

	@Test
	public void testValidateProxyConfigHubUrlIgnored() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setIgnoredProxyHosts("google");

		final HubProxyInfo proxyInfo = builder.build();
		final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));

		assertFalse(useProxy);
	}

	@Test
	public void testValidateProxyConfigHubUrlNotIgnored() throws Exception {
		final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
		builder.setIgnoredProxyHosts("test");

		final HubProxyInfo proxyInfo = builder.build();
		final boolean useProxy = proxyInfo.shouldUseProxyForUrl(new URL("https://google.com"));

		assertTrue(useProxy);
	}
}
