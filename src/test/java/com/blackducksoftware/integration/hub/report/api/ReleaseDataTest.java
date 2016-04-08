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

public class ReleaseDataTest {

    @Test
    public void testReleaseData() {
        final String id1 = "Id1";
        final String version1 = "version1";

        final String id2 = "Id2";
        final String version2 = "version2";

        ReleaseData item1 = new ReleaseData(id1, version1);
        ReleaseData item2 = new ReleaseData(id2, version2);
        ReleaseData item3 = new ReleaseData(id1, version1);

        assertEquals(id1, item1.getId());
        assertEquals(version1, item1.getVersion());

        assertEquals(id2, item2.getId());
        assertEquals(version2, item2.getVersion());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ReleaseData.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ReleaseData [id=");
        builder.append(item1.getId());
        builder.append(", version=");
        builder.append(item1.getVersion());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
