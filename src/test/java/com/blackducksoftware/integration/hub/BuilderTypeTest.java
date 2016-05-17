/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
