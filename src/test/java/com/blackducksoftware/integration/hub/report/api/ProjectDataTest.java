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
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ProjectDataTest {

    @Test
    public void testProjectData() {
        final String id1 = "Id1";
        final String name1 = "name1";
        final Boolean restructured1 = true;

        final String id2 = "Id2";
        final String name2 = "name2";
        final Boolean restructured2 = false;

        ProjectData item1 = new ProjectData(id1, name1, restructured1);
        ProjectData item2 = new ProjectData(id2, name2, restructured2);
        ProjectData item3 = new ProjectData(id1, name1, restructured1);

        assertEquals(id1, item1.getId());
        assertEquals(name1, item1.getName());
        assertEquals(restructured1, item1.getRestructured());

        assertEquals(id2, item2.getId());
        assertEquals(name2, item2.getName());
        assertEquals(restructured2, item2.getRestructured());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ProjectData.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ProjectData [id=");
        builder.append(item1.getId());
        builder.append(", name=");
        builder.append(item1.getName());
        builder.append(", restructured=");
        builder.append(item1.getRestructured());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
