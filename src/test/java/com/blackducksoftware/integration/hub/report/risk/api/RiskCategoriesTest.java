package com.blackducksoftware.integration.hub.report.risk.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RiskCategoriesTest {

    @Test
    public void testRiskCounts() {
        final RiskCounts counts1 = new RiskCounts(1, 1, 1, 1, 1);
        final RiskCounts counts2 = new RiskCounts(2, 2, 2, 2, 2);
        final RiskCounts counts3 = new RiskCounts(1, 2, 3, 1, 2);

        RiskCategories item1 = new RiskCategories(counts1, counts2, counts3, counts1, counts1);
        RiskCategories item2 = new RiskCategories(counts3, counts2, counts1, counts3, counts2);
        RiskCategories item3 = new RiskCategories(counts1, counts2, counts3, counts1, counts1);

        assertEquals(counts1, item1.getVULNERABILITY());
        assertEquals(counts2, item1.getACTIVITY());
        assertEquals(counts3, item1.getVERSION());
        assertEquals(counts1, item1.getLICENSE());
        assertEquals(counts1, item1.getOPERATIONAL());

        assertEquals(counts3, item2.getVULNERABILITY());
        assertEquals(counts2, item2.getACTIVITY());
        assertEquals(counts1, item2.getVERSION());
        assertEquals(counts3, item2.getLICENSE());
        assertEquals(counts2, item2.getOPERATIONAL());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("RiskCategory [VULNERABILITY=");
        builder.append(item1.getVULNERABILITY());
        builder.append(", ACTIVITY=");
        builder.append(item1.getACTIVITY());
        builder.append(", VERSION=");
        builder.append(item1.getVERSION());
        builder.append(", LICENSE=");
        builder.append(item1.getLICENSE());
        builder.append(", OPERATIONAL=");
        builder.append(item1.getOPERATIONAL());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
