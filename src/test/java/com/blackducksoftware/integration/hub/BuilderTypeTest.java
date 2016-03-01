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
