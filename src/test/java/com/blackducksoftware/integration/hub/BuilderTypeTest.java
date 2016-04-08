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
package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class BuilderTypeTest {

    @Test
    public void testGetBuilderType() {
        assertEquals(BuilderType.UNKNOWN_BUILDER, BuilderType.getBuilderType("Fake"));
        assertEquals(BuilderType.ANT, BuilderType.getBuilderType(BuilderType.ANT.toString().toLowerCase()));
        assertEquals(BuilderType.ANT, BuilderType.getBuilderType(BuilderType.ANT.toString()));
        assertEquals(BuilderType.MAVEN, BuilderType.getBuilderType(BuilderType.MAVEN.toString().toLowerCase()));
        assertEquals(BuilderType.MAVEN, BuilderType.getBuilderType(BuilderType.MAVEN.toString()));
        assertEquals(BuilderType.GRADLE, BuilderType.getBuilderType(BuilderType.GRADLE.toString().toLowerCase()));
        assertEquals(BuilderType.GRADLE, BuilderType.getBuilderType(BuilderType.GRADLE.toString()));
    }

    @Test
    public void testGetBuilderTypes() {
        List<BuilderType> builderTypes = BuilderType.getBuilderTypes();
        assertTrue(builderTypes.contains(BuilderType.ANT));
        assertTrue(builderTypes.contains(BuilderType.MAVEN));
        assertTrue(builderTypes.contains(BuilderType.GRADLE));
        assertTrue(builderTypes.contains(BuilderType.UNKNOWN_BUILDER));
    }

}
