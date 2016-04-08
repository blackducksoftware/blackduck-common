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
package com.blackducksoftware.integration.hub.report.risk.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

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

        EqualsVerifier.forClass(RiskProfile.class).suppress(Warning.STRICT_INHERITANCE).verify();

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
