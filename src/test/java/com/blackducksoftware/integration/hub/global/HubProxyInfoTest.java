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

import org.junit.Test;

import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class HubProxyInfoTest {
	@Test
	public void testHubProxyInfo() throws Exception {
		final String host1 = "host1";
		final int port1 = 1;
		final String noProxyHosts1 = "noProxyHosts1";
		final String proxyUsername1 = "proxyUsername1";
		final String proxyPassword1 = "proxyPassword1";

		final String host2 = "host2";
		final int port2 = 2;
		final String noProxyHosts2 = "noProxyHosts2";
		final String proxyUsername2 = "proxyUsername2";
		final String proxyPassword2 = "proxyPassword2";

		final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
		credBuilder.setUsername(proxyUsername1);
		credBuilder.setPassword(proxyPassword1);
		final HubProxyInfo item1 = new HubProxyInfo(host1, port1, credBuilder.build().getConstructedObject(),
				noProxyHosts1);
		credBuilder.setUsername(proxyUsername2);
		credBuilder.setPassword(proxyPassword2);
		final HubProxyInfo item2 = new HubProxyInfo(host2, port2, credBuilder.build().getConstructedObject(),
				noProxyHosts2);
		credBuilder.setUsername(proxyUsername1);
		credBuilder.setPassword(proxyPassword1);
		final HubProxyInfo item3 = new HubProxyInfo(host1, port1, credBuilder.build().getConstructedObject(),
				noProxyHosts1);

		assertEquals(host1, item1.getHost());
		assertEquals(port1, item1.getPort());
		assertEquals(noProxyHosts1, item1.getIgnoredProxyHosts());
		assertEquals(proxyUsername1, item1.getUsername());
		assertEquals(PasswordEncrypter.encrypt(proxyPassword1), item1.getEncryptedPassword());
		assertEquals("**************", item1.getMaskedPassword());

		assertEquals(host2, item2.getHost());
		assertEquals(port2, item2.getPort());
		assertEquals(noProxyHosts2, item2.getIgnoredProxyHosts());
		assertEquals(proxyUsername2, item2.getUsername());
		assertEquals(PasswordEncrypter.encrypt(proxyPassword2), item2.getEncryptedPassword());
		assertEquals("**************", item2.getMaskedPassword());

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
		builder.append(", username=");
		builder.append(item1.getUsername());
		builder.append(", encryptedPassword=");
		builder.append(item1.getEncryptedPassword());
		builder.append(", actualPasswordLength=");
		builder.append(item1.getActualPasswordLength());
		builder.append(", ignoredProxyHosts=");
		builder.append(item1.getIgnoredProxyHosts());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

	@Test
	public void testShouldUseProxyForURL() {

	}

	@Test
	public void testSetDefaultAuthenticator() {

	}

}
