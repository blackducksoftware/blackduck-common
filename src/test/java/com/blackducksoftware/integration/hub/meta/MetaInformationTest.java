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

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class MetaInformationTest {


	@Test
	public void testMetaInformation(){
		final String rel1 = "rel1";
		final String href1 = "href1";

		final String rel2 = "rel2";
		final String href2 = "href2";

		final MetaLink link1 = new MetaLink(rel1, href1);
		final List<MetaLink> links1 = new ArrayList<MetaLink>();
		links1.add(link1);

		final MetaLink link2 = new MetaLink(rel2, href2);
		final List<MetaLink> links2 = new ArrayList<MetaLink>();
		links2.add(link2);

		final List<String> allow1 = new ArrayList<String>();
		allow1.add("GET");

		final List<String> allow2 = new ArrayList<String>();
		allow2.add("PUT");


		final MetaInformation item1 = new MetaInformation(allow1, href1, links1);
		final MetaInformation item2 = new MetaInformation(allow2, href2, links2);
		final MetaInformation item3 = new MetaInformation(allow1, href1, links1);

		assertEquals(allow1, item1.getAllow());
		assertEquals(href1, item1.getHref());
		assertEquals(links1, item1.getLinks());
		assertEquals(allow2, item2.getAllow());
		assertEquals(href2, item2.getHref());
		assertEquals(links2, item2.getLinks());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(MetaInformation.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());


		final StringBuilder builder = new StringBuilder();
		builder.append("MetaInformation [allow=");
		builder.append(item1.getAllow());
		builder.append(", href=");
		builder.append(item1.getHref());
		builder.append(", links=");
		builder.append(item1.getLinks());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());

	}
}
