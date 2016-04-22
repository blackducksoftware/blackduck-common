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

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class HubProxyInfoTest {

	@Test
	public void testHubProxyInfo() {
		final String host1 = "host1";
		final Integer port1 = 1;
		final String noProxyHosts1 = "noProxyHosts1";
		final List<Pattern> noProxyHostsPatterns1 = null;
		final String proxyUsername1 = "proxyUsername1";
		final String proxyPassword1 = "proxyPassword1";
		final boolean hubUrlIgnored1 = true;

		final String host2 = "host2";
		final Integer port2 = 2;
		final String noProxyHosts2 = "noProxyHosts2";
		final List<Pattern> noProxyHostsPatterns2 = null;
		final String proxyUsername2 = "proxyUsername2";
		final String proxyPassword2 = "proxyPassword2";
		final boolean hubUrlIgnored2 = false;


		final HubProxyInfo item1 = new HubProxyInfo(host1, port1, noProxyHosts1, noProxyHostsPatterns1, proxyUsername1,
				proxyPassword1, hubUrlIgnored1);
		final HubProxyInfo item2 = new HubProxyInfo(host2, port2, noProxyHosts2, noProxyHostsPatterns2, proxyUsername2,
				proxyPassword2, hubUrlIgnored2);
		final HubProxyInfo item3 = new HubProxyInfo(host1, port1, noProxyHosts1, noProxyHostsPatterns1, proxyUsername1,
				proxyPassword1, hubUrlIgnored1);

		assertEquals(host1, item1.getHost());
		assertEquals(port1, item1.getPort());
		assertEquals(noProxyHosts1, item1.getIgnoredProxyHosts());
		assertEquals(noProxyHostsPatterns1, item1.getIgnoredProxyHostPatterns());
		assertEquals(proxyUsername1, item1.getProxyUsername());
		assertEquals(proxyPassword1, item1.getProxyPassword());

		assertEquals(host2, item2.getHost());
		assertEquals(port2, item2.getPort());
		assertEquals(noProxyHosts2, item2.getIgnoredProxyHosts());
		assertEquals(noProxyHostsPatterns2, item2.getIgnoredProxyHostPatterns());
		assertEquals(proxyUsername2, item2.getProxyUsername());
		assertEquals(proxyPassword2, item2.getProxyPassword());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(HubProxyInfo.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("HubProxyInfo [host=");
		builder.append(item1.getHost());
		builder.append(", port=");
		builder.append(item1.getPort());
		builder.append(", proxyUsername=");
		builder.append(item1.getProxyUsername());
		builder.append(", proxyPassword=");
		builder.append(item1.getProxyPassword());
		builder.append(", ignoredProxyHosts=");
		builder.append(item1.getIgnoredProxyHosts());
		builder.append(", ignoredProxyHostPatterns=");
		builder.append(item1.getIgnoredProxyHostPatterns());
		builder.append(", hubUrlIgnored=");
		builder.append(item1.isHubUrlIgnored());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());

	}
}
