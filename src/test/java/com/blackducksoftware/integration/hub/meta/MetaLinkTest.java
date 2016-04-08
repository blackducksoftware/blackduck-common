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
package com.blackducksoftware.integration.hub.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class MetaLinkTest {


	@Test
	public void testMetaLink(){
		final String rel1 = "rel1";
		final String href1 = "href1";

		final String rel2 = "rel2";
		final String href2 = "href2";

		final MetaLink item1 = new MetaLink(rel1, href1);
		final MetaLink item2 = new MetaLink(rel2, href2);
		final MetaLink item3 = new MetaLink(rel1, href1);

		assertEquals(rel1, item1.getRel());
		assertEquals(href1, item1.getHref());
		assertEquals(rel2, item2.getRel());
		assertEquals(href2, item2.getHref());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(MetaLink.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("MetaLink [rel=");
		builder.append(item1.getRel());
		builder.append(", href=");
		builder.append(item1.getHref());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}
}
