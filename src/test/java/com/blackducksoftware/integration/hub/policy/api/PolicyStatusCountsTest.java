package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class PolicyStatusCountsTest {

    @Test
    public void testPolicyStatusCounts() {
        final String IN_VIOLATION1 = "IN_VIOLATION1";
        final String IN_VIOLATION_OVERRIDEN1 = "IN_VIOLATION_OVERRIDEN1";
        final String NOT_IN_VIOLATION1 = "NOT_IN_VIOLATION1";

        final String IN_VIOLATION2 = "IN_VIOLATION2";
        final String IN_VIOLATION_OVERRIDEN2 = "IN_VIOLATION_OVERRIDEN2";
        final String NOT_IN_VIOLATION2 = "NOT_IN_VIOLATION2";

        PolicyStatusCounts item1 = new PolicyStatusCounts(IN_VIOLATION1, IN_VIOLATION_OVERRIDEN1, NOT_IN_VIOLATION1);
        PolicyStatusCounts item2 = new PolicyStatusCounts(IN_VIOLATION2, IN_VIOLATION_OVERRIDEN2, NOT_IN_VIOLATION2);
        PolicyStatusCounts item3 = new PolicyStatusCounts(IN_VIOLATION1, IN_VIOLATION_OVERRIDEN1, NOT_IN_VIOLATION1);

        assertEquals(IN_VIOLATION1, item1.getIN_VIOLATION());
        assertEquals(IN_VIOLATION_OVERRIDEN1, item1.getIN_VIOLATION_OVERRIDEN());
        assertEquals(NOT_IN_VIOLATION1, item1.getNOT_IN_VIOLATION());

        assertEquals(IN_VIOLATION2, item2.getIN_VIOLATION());
        assertEquals(IN_VIOLATION_OVERRIDEN2, item2.getIN_VIOLATION_OVERRIDEN());
        assertEquals(NOT_IN_VIOLATION2, item2.getNOT_IN_VIOLATION());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(PolicyStatusCounts.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("PolicyStatusCounts [IN_VIOLATION=");
        builder.append(item1.getIN_VIOLATION());
        builder.append(", IN_VIOLATION_OVERRIDEN=");
        builder.append(item1.getIN_VIOLATION_OVERRIDEN());
        builder.append(", NOT_IN_VIOLATION=");
        builder.append(item1.getNOT_IN_VIOLATION());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
