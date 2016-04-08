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
package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ComponentVersionStatusCountTest {

    @Test
    public void testComponentVersionStatusCount() {
        final String name1 = "name1";
        final int value1 = 324;

        final String name2 = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
        final int value2 = 0;

        ComponentVersionStatusCount item1 = new ComponentVersionStatusCount(name1, value1);
        ComponentVersionStatusCount item2 = new ComponentVersionStatusCount(name2, value2);
        ComponentVersionStatusCount item3 = new ComponentVersionStatusCount(name1, value1);

        assertEquals(name1, item1.getName());
        assertEquals(PolicyStatusEnum.UNKNOWN, item1.getPolicyStatusFromName());
        assertEquals(value1, item1.getValue());

        assertEquals(name2, item2.getName());
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, item2.getPolicyStatusFromName());
        assertEquals(value2, item2.getValue());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ComponentVersionStatusCount.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ComponentVersionStatusCount [name=");
        builder.append(name1);
        builder.append(", value=");
        builder.append(value1);
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
