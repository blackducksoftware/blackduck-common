package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class PolicyRuleTest {

	@Test
	public void testPolicyRule() {
		final String name1 = "name1";
		final String description1 = "description1";
		final Boolean enabled1 = true;
		final Boolean overridable1 = true;
		final String createdAt1 = "createdAt1";
		final String createdBy1 = "createdBy1";
		final String updatedAt1 = "updatedAt1";
		final String updatedBy1 = "updatedBy1";

		final String allow1 = "allow1";
		final List<String> allows1 = new ArrayList<String>();
		allows1.add(allow1);
		final String href1 = "href1";
		final MetaLink link1 = new MetaLink("rel1", "link1");
		final List<MetaLink> links1 = new ArrayList<MetaLink>();
		links1.add(link1);
		final MetaInformation _meta1 = new MetaInformation(allows1, href1, links1);

		final String name2 = "name2";
		final String description2 = "description2";
		final Boolean enabled2 = false;
		final Boolean overridable2 = false;
		final DateTime createdAt2 = new DateTime();
		final String createdBy2 = "createdBy2";
		final DateTime updatedAt2 = new DateTime();
		final String updatedBy2 = "updatedBy2";

		final String allow2 = "allow2";
		final List<String> allows2 = new ArrayList<String>();
		allows2.add(allow2);
		final String href2 = "href2";
		final MetaLink link2 = new MetaLink("rel2", "link2");
		final List<MetaLink> links2 = new ArrayList<MetaLink>();
		links2.add(link2);
		final MetaInformation _meta2 = new MetaInformation(allows2, href2, links2);

		final PolicyRule item1 = new PolicyRule(_meta1, name1, description1, enabled1, overridable1,
				createdAt1, createdBy1, updatedAt1, updatedBy1);
		final PolicyRule item2 = new PolicyRule(_meta2, name2, description2, enabled2, overridable2,
				createdAt2.toString(), createdBy2, updatedAt2.toString(), updatedBy2);
		final PolicyRule item3 = new PolicyRule(_meta1, name1, description1, enabled1, overridable1,
				createdAt1, createdBy1, updatedAt1, updatedBy1);
		final PolicyRule item4 = new PolicyRule(null, null, null, null, null, null, null, null, null);

		assertNull(item4.getCreatedAtTime());
		assertNull(item4.getUpdatedAtTime());

		assertEquals(_meta1, item1.getMeta());
		assertEquals(name1, item1.getName());
		assertEquals(description1, item1.getDescription());
		assertEquals(enabled1, item1.getEnabled());
		assertEquals(overridable1, item1.getOverridable());
		assertEquals(createdAt1, item1.getCreatedAt());
		assertEquals(createdBy1, item1.getCreatedBy());
		assertEquals(updatedAt1, item1.getUpdatedAt());
		assertEquals(updatedBy1, item1.getUpdatedBy());
		assertNull(item1.getCreatedAtTime());
		assertNull(item1.getUpdatedAtTime());

		assertEquals(_meta2, item2.getMeta());
		assertEquals(name2, item2.getName());
		assertEquals(description2, item2.getDescription());
		assertEquals(enabled2, item2.getEnabled());
		assertEquals(overridable2, item2.getOverridable());
		assertEquals(createdAt2.toString(), item2.getCreatedAt());
		assertEquals(createdBy2, item2.getCreatedBy());
		assertEquals(updatedAt2.toString(), item2.getUpdatedAt());
		assertEquals(updatedBy2, item2.getUpdatedBy());
		assertEquals(createdAt2, item2.getCreatedAtTime());
		assertEquals(updatedAt2, item2.getUpdatedAtTime());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(PolicyRule.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyRule [name=");
		builder.append(item1.getName());
		builder.append(", description=");
		builder.append(item1.getDescription());
		builder.append(", enabled=");
		builder.append(item1.getEnabled());
		builder.append(", overridable=");
		builder.append(item1.getOverridable());
		builder.append(", createdAt=");
		builder.append(item1.getCreatedAt());
		builder.append(", createdBy=");
		builder.append(item1.getCreatedBy());
		builder.append(", updatedAt=");
		builder.append(item1.getUpdatedAt());
		builder.append(", updatedBy=");
		builder.append(item1.getUpdatedBy());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
