package com.blackducksoftware.integration.hub.report.risk.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class RiskCountsTest {

    @Test
    public void testRiskCounts() {
        final int HIGH1 = 1;
        final int MEDIUM1 = 1;
        final int LOW1 = 1;
        final int OK1 = 1;
        final int UNKNOWN1 = 1;

        final int HIGH2 = 2;
        final int MEDIUM2 = 2;
        final int LOW2 = 2;
        final int OK2 = 2;
        final int UNKNOWN2 = 2;

        RiskCounts item1 = new RiskCounts(HIGH1, MEDIUM1, LOW1, OK1, UNKNOWN1);
        RiskCounts item2 = new RiskCounts(HIGH2, MEDIUM2, LOW2, OK2, UNKNOWN2);
        RiskCounts item3 = new RiskCounts(HIGH1, MEDIUM1, LOW1, OK1, UNKNOWN1);

        assertEquals(HIGH1, item1.getHIGH());
        assertEquals(MEDIUM1, item1.getMEDIUM());
        assertEquals(LOW1, item1.getLOW());
        assertEquals(OK1, item1.getOK());
        assertEquals(UNKNOWN1, item1.getUNKNOWN());

        assertEquals(HIGH2, item2.getHIGH());
        assertEquals(MEDIUM2, item2.getMEDIUM());
        assertEquals(LOW2, item2.getLOW());
        assertEquals(OK2, item2.getOK());
        assertEquals(UNKNOWN2, item2.getUNKNOWN());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(RiskCounts.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("RiskCounts [HIGH=");
        builder.append(item1.getHIGH());
        builder.append(", MEDIUM=");
        builder.append(item1.getMEDIUM());
        builder.append(", LOW=");
        builder.append(item1.getLOW());
        builder.append(", OK=");
        builder.append(item1.getOK());
        builder.append(", UNKNOWN=");
        builder.append(item1.getUNKNOWN());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
