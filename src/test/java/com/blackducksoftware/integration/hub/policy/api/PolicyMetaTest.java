package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class PolicyMetaTest {

    @Test
    public void testPolicyMeta() {
        final String allow1 = "allow1";
        final List<String> allows1 = new ArrayList<String>();
        allows1.add(allow1);
        final String href1 = "href1";
        final String link1 = "link1";
        final List<String> links1 = new ArrayList<String>();
        links1.add(link1);

        final String allow2 = "allow2";
        final List<String> allows2 = new ArrayList<String>();
        allows2.add(allow2);
        final String href2 = "href2";
        final String link2 = "link2";
        final List<String> links2 = new ArrayList<String>();
        links1.add(link2);

        PolicyMeta item1 = new PolicyMeta(allows1, href1, links1);
        PolicyMeta item2 = new PolicyMeta(allows2, href2, links2);
        PolicyMeta item3 = new PolicyMeta(allows1, href1, links1);

        assertEquals(allow1, item1.getAllow());
        assertEquals(href1, item1.getHref());
        assertEquals(links1, item1.getLinks());

        assertEquals(allow2, item2.getAllow());
        assertEquals(href2, item2.getHref());
        assertEquals(links2, item2.getLinks());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(PolicyMeta.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("PolicyMeta [allow=");
        builder.append(item1.getAllow());
        builder.append(", href=");
        builder.append(item1.getHref());
        builder.append(", links=");
        builder.append(item1.getLinks());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
