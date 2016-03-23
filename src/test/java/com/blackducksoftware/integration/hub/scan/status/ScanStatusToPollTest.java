package com.blackducksoftware.integration.hub.scan.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ScanStatusToPollTest {

    @Test
    public void testScanStatusToPoll() {
        final String status1 = "fakeStatus1";
        final String href1 = "href1";
        final ScanStatusMeta meta1 = new ScanStatusMeta(href1);

        final String status2 = ScanStatus.COMPLETE.name();
        final String href2 = "href2";
        final ScanStatusMeta meta2 = new ScanStatusMeta(href2);

        ScanStatusToPoll item1 = new ScanStatusToPoll(status1, meta1);
        ScanStatusToPoll item2 = new ScanStatusToPoll(status2, meta2);
        ScanStatusToPoll item3 = new ScanStatusToPoll(status1, meta1);

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

        StringBuilder builder = new StringBuilder();
        builder.append("ScanStatusCliOutput [status=");
        builder.append(status1);
        builder.append(", _meta=");
        builder.append(meta1);
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
