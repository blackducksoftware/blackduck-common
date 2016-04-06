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
