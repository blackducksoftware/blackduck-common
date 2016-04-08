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
package com.blackducksoftware.integration.hub.scan.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ScanStatusToPollTest {

	@Test
	public void testScanStatusToPoll() {
		final String status1 = "fakeStatus1";
		final String href1 = "href1";
		final MetaInformation meta1 = new MetaInformation(null, href1, null);

		final String status2 = ScanStatus.COMPLETE.name();
		final String href2 = "href2";
		final MetaInformation meta2 = new MetaInformation(null,href2, null);

		final ScanStatusToPoll item1 = new ScanStatusToPoll(status1, meta1);
		final ScanStatusToPoll item2 = new ScanStatusToPoll(status2, meta2);
		final ScanStatusToPoll item3 = new ScanStatusToPoll(status1, meta1);

		assertEquals(status1, item1.getStatus());
		assertEquals(ScanStatus.UNKNOWN, item1.getStatusEnum());
		assertEquals(meta1, item1.get_meta());

		assertEquals(status2, item2.getStatus());
		assertEquals(ScanStatus.COMPLETE, item2.getStatusEnum());
		assertEquals(meta2, item2.get_meta());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(ScanStatusToPoll.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("ScanStatusToPoll [status=");
		builder.append(status1);
		builder.append(", _meta=");
		builder.append(meta1);
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
