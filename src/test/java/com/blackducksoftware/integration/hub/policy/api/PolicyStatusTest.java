package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.joda.time.DateTime;
import org.junit.Test;

public class PolicyStatusTest {

    @Test
    public void testPolicyStatus() {
        final String overallStatus1 = "status1";
        final String updatedAt1 = "time1";
        final String IN_VIOLATION1 = "IN_VIOLATION1";
        final String IN_VIOLATION_OVERRIDEN1 = "IN_VIOLATION_OVERRIDEN1";
        final String NOT_IN_VIOLATION1 = "NOT_IN_VIOLATION1";
        PolicyStatusCounts statusCounts1 = new PolicyStatusCounts(IN_VIOLATION1, IN_VIOLATION_OVERRIDEN1, NOT_IN_VIOLATION1);
        final String allow1 = "allow1";
        final List<String> allows1 = new ArrayList<String>();
        allows1.add(allow1);
        final String href1 = "href1";
        final String link1 = "link1";
        final List<String> links1 = new ArrayList<String>();
        links1.add(link1);
        PolicyMeta _meta1 = new PolicyMeta(allows1, href1, links1);

        final String overallStatus2 = PolicyStatusEnum.IN_VIOLATION.name();
        final String updatedAt2 = new DateTime().toString();
        final String IN_VIOLATION2 = "IN_VIOLATION2";
        final String IN_VIOLATION_OVERRIDEN2 = "IN_VIOLATION_OVERRIDEN2";
        final String NOT_IN_VIOLATION2 = "NOT_IN_VIOLATION2";
        PolicyStatusCounts statusCounts2 = new PolicyStatusCounts(IN_VIOLATION2, IN_VIOLATION_OVERRIDEN2, NOT_IN_VIOLATION2);
        final String allow2 = "allow2";
        final List<String> allows2 = new ArrayList<String>();
        allows2.add(allow2);
        final String href2 = "href2";
        final String link2 = "link2";
        final List<String> links2 = new ArrayList<String>();
        links2.add(link2);
        PolicyMeta _meta2 = new PolicyMeta(allows2, href2, links2);

        PolicyStatus item1 = new PolicyStatus(overallStatus1, updatedAt1, statusCounts1, _meta1);
        PolicyStatus item2 = new PolicyStatus(overallStatus2, updatedAt2, statusCounts2, _meta2);
        PolicyStatus item3 = new PolicyStatus(overallStatus1, updatedAt1, statusCounts1, _meta1);
        PolicyStatus item4 = new PolicyStatus("", null, null, null);

        assertEquals(overallStatus1, item1.getOverallStatus());
        assertEquals(PolicyStatusEnum.UNKNOWN, item1.getOverallStatusEnum());
        assertEquals(updatedAt1, item1.getUpdatedAt());
        assertNull(item1.getUpdatedAtTime());
        assertEquals(statusCounts1, item1.getStatusCounts());
        assertEquals(_meta1, item1.get_meta());

        assertEquals(overallStatus2, item2.getOverallStatus());
        assertEquals(PolicyStatusEnum.IN_VIOLATION, item2.getOverallStatusEnum());
        assertEquals(updatedAt2, item2.getUpdatedAt());
        assertEquals(updatedAt2, item2.getUpdatedAtTime().toString());
        assertEquals(statusCounts2, item2.getStatusCounts());
        assertEquals(_meta2, item2.get_meta());

        assertEquals(PolicyStatusEnum.UNKNOWN, item4.getOverallStatusEnum());
        assertNull(item4.getUpdatedAtTime());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(PolicyStatus.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("PolicyStatus [overallStatus=");
        builder.append(item1.getOverallStatus());
        builder.append(", updatedAt=");
        builder.append(item1.getUpdatedAt());
        builder.append(", statusCounts=");
        builder.append(item1.getStatusCounts());
        builder.append(", _meta=");
        builder.append(item1.get_meta());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
