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
        final String name1 = "name1";
        final int value1 = 3214;
        ComponentVersionStatusCount statusCount1 = new ComponentVersionStatusCount(name1, value1);
        List<ComponentVersionStatusCount> counts1 = new ArrayList<ComponentVersionStatusCount>();
        counts1.add(statusCount1);
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
        final String name2 = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
        final int value2 = 0;
        ComponentVersionStatusCount statusCount2 = new ComponentVersionStatusCount(name2, value2);
        List<ComponentVersionStatusCount> counts2 = new ArrayList<ComponentVersionStatusCount>();
        counts2.add(statusCount2);
        final String allow2 = "allow2";
        final List<String> allows2 = new ArrayList<String>();
        allows2.add(allow2);
        final String href2 = "href2";
        final String link2 = "link2";
        final List<String> links2 = new ArrayList<String>();
        links2.add(link2);
        PolicyMeta _meta2 = new PolicyMeta(allows2, href2, links2);

        PolicyStatus item1 = new PolicyStatus(overallStatus1, updatedAt1, counts1, _meta1);
        PolicyStatus item2 = new PolicyStatus(overallStatus2, updatedAt2, counts2, _meta2);
        PolicyStatus item3 = new PolicyStatus(overallStatus1, updatedAt1, counts1, _meta1);
        PolicyStatus item4 = new PolicyStatus("", null, null, null);

        assertEquals(overallStatus1, item1.getOverallStatus());
        assertEquals(PolicyStatusEnum.UNKNOWN, item1.getOverallStatusEnum());
        assertEquals(updatedAt1, item1.getUpdatedAt());
        assertNull(item1.getUpdatedAtTime());
        assertEquals(counts1, item1.getComponentVersionStatusCounts());
        assertEquals(_meta1, item1.get_meta());

        assertEquals(overallStatus2, item2.getOverallStatus());
        assertEquals(PolicyStatusEnum.IN_VIOLATION, item2.getOverallStatusEnum());
        assertEquals(updatedAt2, item2.getUpdatedAt());
        assertEquals(updatedAt2, item2.getUpdatedAtTime().toString());
        assertEquals(counts2, item2.getComponentVersionStatusCounts());
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
        builder.append(", componentVersionStatusCounts=");
        builder.append(item1.getComponentVersionStatusCounts());
        builder.append(", _meta=");
        builder.append(item1.get_meta());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

    @Test
    public void testGetCountInViolation() {
        PolicyStatus status = new PolicyStatus(null, null, null, null);

        assertNull(status.getCountInViolation());

        String name = "name";
        int value = 346;
        ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
        List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountInViolation());

        name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountInViolation());

        name = PolicyStatusEnum.IN_VIOLATION.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertEquals(statusCount, status.getCountInViolation());

    }

    @Test
    public void testGetCountNotInViolation() {
        PolicyStatus status = new PolicyStatus(null, null, null, null);

        assertNull(status.getCountNotInViolation());

        String name = "name";
        int value = 346;
        ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
        List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountNotInViolation());

        name = PolicyStatusEnum.IN_VIOLATION.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountNotInViolation());

        name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertEquals(statusCount, status.getCountNotInViolation());

    }

    @Test
    public void testGetCountInViolationOveridden() {
        PolicyStatus status = new PolicyStatus(null, null, null, null);

        assertNull(status.getCountInViolationOveridden());

        String name = "name";
        int value = 346;
        ComponentVersionStatusCount statusCount = new ComponentVersionStatusCount(name, value);
        List<ComponentVersionStatusCount> counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountInViolationOveridden());

        name = PolicyStatusEnum.NOT_IN_VIOLATION.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertNull(status.getCountInViolationOveridden());

        name = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
        value = 435;
        statusCount = new ComponentVersionStatusCount(name, value);
        counts = new ArrayList<ComponentVersionStatusCount>();
        counts.add(statusCount);

        status = new PolicyStatus(null, null, counts, null);

        assertEquals(statusCount, status.getCountInViolationOveridden());

    }

}
