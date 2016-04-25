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

import java.net.URL;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class HubServerConfigTest {

	@Test
	public void testHubServerConfig() throws Exception {
		final URL hubUrl1 = new URL("https://www.blackducksoftware.com/");
		final Integer timeout1 = 1;

		final HubCredentials credentials1 = new HubCredentials("hubUser1", "hubPass1");

		final String host1 = "host1";
		final Integer port1 = 1;
		final String noProxyHosts1 = "noProxyHosts1";
		final String proxyUsername1 = "proxyUsername1";
		final String proxyPassword1 = "proxyPassword1";

		final HubProxyInfo proxy1 = new HubProxyInfo(host1, port1, noProxyHosts1, proxyUsername1, proxyPassword1);

		final URL hubUrl2 = new URL("http://google.com");
		final Integer timeout2 = 2;

		final String host2 = "host2";
		final Integer port2 = 2;
		final String noProxyHosts2 = "noProxyHosts2";
		final String proxyUsername2 = "proxyUsername2";
		final String proxyPassword2 = "proxyPassword2";

		final HubCredentials credentials2 = new HubCredentials("hubUser2", "hubPass2");
		final HubProxyInfo proxy2 = new HubProxyInfo(host2, port2, noProxyHosts2, proxyUsername2, proxyPassword2);

		final HubServerConfig item1 = new HubServerConfig(hubUrl1, timeout1, credentials1, proxy1);
		final HubServerConfig item2 = new HubServerConfig(hubUrl2, timeout2, credentials2, proxy2);
		final HubServerConfig item3 = new HubServerConfig(hubUrl1, timeout1, credentials1, proxy1);

		assertEquals(hubUrl1, item1.getHubUrl());
		assertEquals(timeout1, Integer.valueOf(item1.getTimeout()));
		assertEquals(credentials1, item1.getGlobalCredentials());
		assertEquals(proxy1, item1.getProxyInfo());

		assertEquals(hubUrl2, item2.getHubUrl());
		assertEquals(timeout2, Integer.valueOf(item2.getTimeout()));
		assertEquals(credentials2, item2.getGlobalCredentials());
		assertEquals(proxy2, item2.getProxyInfo());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(HubServerConfig.class).withPrefabValues(URL.class, hubUrl1, hubUrl2)
				.suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("HubServerConfig [hubUrl=");
		builder.append(item1.getHubUrl());
		builder.append(", timeout=");
		builder.append(item1.getTimeout());
		builder.append(", hubCredentials=");
		builder.append(item1.getGlobalCredentials());
		builder.append(", proxyInfo=");
		builder.append(item1.getProxyInfo());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
