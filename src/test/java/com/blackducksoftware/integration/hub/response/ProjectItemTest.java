package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;

public class ProjectItemTest {

    @Test
    public void testProjectItem() {
        final String id1 = "ID1";
        final Boolean kb1 = true;
        final String name1 = "Name1";
        final Boolean restructured1 = true;
        final String canonicalReleaseId1 = "canonRel1";
        final Boolean internal1 = true;
        final Boolean openSource1 = true;
        final ReleaseItem release1 = new ReleaseItem();

        final String id2 = "ID2";
        final Boolean kb2 = false;
        final String name2 = "Name2";
        final Boolean restructured2 = false;
        final String canonicalReleaseId2 = "canonRel2";
        final Boolean internal2 = false;
        final Boolean openSource2 = false;
        final ReleaseItem release2 = new ReleaseItem("test", true, true, "test", "test", "test", "test", "test", "test", "test", "test", "test", "test");

        final ReleaseItem release3 = new ReleaseItem("test", true, true, "test", "test", "test", "test", "NOTEQUAL", "test", "test", "test", "test", "test");

        ProjectItem item1 = new ProjectItem(id1, kb1, name1, restructured1, canonicalReleaseId1, internal1, openSource1, release1);
        ProjectItem item2 = new ProjectItem(id2, kb2, name2, restructured2, canonicalReleaseId2, internal2, openSource2, release2);
        ProjectItem item3 = new ProjectItem(id1, kb1, name1, restructured1, canonicalReleaseId1, internal1, openSource1, release1);
        ProjectItem item4 = new ProjectItem(id2, kb2, name2, restructured2, canonicalReleaseId2, internal2, openSource2, release3);

        ProjectItem item5 = new ProjectItem();
        item5.setId(id1);
        item5.setKb(kb1);
        item5.setName(name1);
        item5.setRestructured(restructured1);
        item5.setCanonicalReleaseId(canonicalReleaseId1);
        item5.setInternal(internal1);
        item5.setOpenSource(openSource1);
        item5.setReleaseItem(release1);

        assertEquals(id1, item5.getId());
        assertEquals(kb1, item5.getKb());
        assertEquals(name1, item5.getName());
        assertEquals(restructured1, item5.getRestructured());
        assertEquals(canonicalReleaseId1, item5.getCanonicalReleaseId());
        assertEquals(internal1, item5.getInternal());
        assertEquals(openSource1, item5.getOpenSource());
        assertEquals(release1, item5.getReleaseItem());

        assertEquals(id1, item1.getId());
        assertEquals(kb1, item1.getKb());
        assertEquals(name1, item1.getName());
        assertEquals(restructured1, item1.getRestructured());
        assertEquals(canonicalReleaseId1, item1.getCanonicalReleaseId());
        assertEquals(internal1, item1.getInternal());
        assertEquals(openSource1, item1.getOpenSource());
        assertEquals(release1, item1.getReleaseItem());

        assertEquals(id2, item2.getId());
        assertEquals(kb2, item2.getKb());
        assertEquals(name2, item2.getName());
        assertEquals(restructured2, item2.getRestructured());
        assertEquals(canonicalReleaseId2, item2.getCanonicalReleaseId());
        assertEquals(internal2, item2.getInternal());
        assertEquals(openSource2, item2.getOpenSource());
        assertEquals(release2, item2.getReleaseItem());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));
        assertTrue(!item2.equals(item4));

        EqualsVerifier.forClass(ProjectItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());
        assertTrue(item2.hashCode() != item4.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ProjectItem [id=");
        builder.append(item1.getId());
        builder.append(", kb=");
        builder.append(item1.getKb());
        builder.append(", name=");
        builder.append(item1.getName());
        builder.append(", restructured=");
        builder.append(item1.getRestructured());
        builder.append(", canonicalReleaseId=");
        builder.append(item1.getCanonicalReleaseId());
        builder.append(", internal=");
        builder.append(item1.getInternal());
        builder.append(", openSource=");
        builder.append(item1.getOpenSource());
        builder.append(", release=");
        builder.append(item1.getReleaseItem());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
