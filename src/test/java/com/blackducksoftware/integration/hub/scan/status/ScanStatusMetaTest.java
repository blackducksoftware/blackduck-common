package com.blackducksoftware.integration.hub.scan.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ScanStatusMetaTest {

    @Test
    public void testScanStatusMeta() {
        final String href1 = "href1";

        final String href2 = "href2";

        ScanStatusMeta item1 = new ScanStatusMeta(href1);
        ScanStatusMeta item2 = new ScanStatusMeta(href2);
        ScanStatusMeta item3 = new ScanStatusMeta(href1);

        assertEquals(href1, item1.getHref());

        assertEquals(href2, item2.getHref());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ScanStatusMeta.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ScanStatusMeta [href=");
        builder.append(href1);
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
