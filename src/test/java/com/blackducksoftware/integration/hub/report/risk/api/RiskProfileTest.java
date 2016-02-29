package com.blackducksoftware.integration.hub.report.risk.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RiskProfileTest {

    @Test
    public void testRiskProfile() {
        final RiskCounts counts1 = new RiskCounts(1, 1, 1, 1, 1);
        final RiskCounts counts2 = new RiskCounts(2, 2, 2, 2, 2);
        final RiskCounts counts3 = new RiskCounts(1, 2, 3, 1, 2);

        final RiskCategories categories1 = new RiskCategories(counts1, counts2, counts3, counts1, counts1);
        final RiskCategories categories2 = new RiskCategories(counts3, counts2, counts1, counts3, counts2);

        final int numberOfItems1 = 1;
        final int numberOfItems2 = 2;

        RiskProfile item1 = new RiskProfile(numberOfItems1, categories1);
        RiskProfile item2 = new RiskProfile(numberOfItems2, categories2);
        RiskProfile item3 = new RiskProfile(numberOfItems1, categories1);

        assertEquals(numberOfItems1, item1.getNumberOfItems());
        assertEquals(categories1, item1.getCategories());

        assertEquals(numberOfItems2, item2.getNumberOfItems());
        assertEquals(categories2, item2.getCategories());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("RiskProfile [numberOfItems=");
        builder.append(numberOfItems1);
        builder.append(", categories=");
        builder.append(categories1);
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
