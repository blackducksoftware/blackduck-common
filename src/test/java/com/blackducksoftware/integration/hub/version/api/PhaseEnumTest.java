/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.version.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

public class PhaseEnumTest {

    @Test
    public void testGetPhaseEnum() {
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum("Fake"));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseEnum(PhaseEnum.ARCHIVED.toString().toLowerCase()));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseEnum(PhaseEnum.ARCHIVED.toString()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseEnum(PhaseEnum.DEPRECATED.toString().toLowerCase()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseEnum(PhaseEnum.DEPRECATED.toString()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseEnum(PhaseEnum.DEVELOPMENT.toString().toLowerCase()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseEnum(PhaseEnum.DEVELOPMENT.toString()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseEnum(PhaseEnum.PLANNING.toString().toLowerCase()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseEnum(PhaseEnum.PLANNING.toString()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseEnum(PhaseEnum.RELEASED.toString().toLowerCase()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseEnum(PhaseEnum.RELEASED.toString()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum(PhaseEnum.UNKNOWNPHASE.toString().toLowerCase()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum(PhaseEnum.UNKNOWNPHASE.toString()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum(null));
    }

    @Test
    public void testGetPhaseEnumByDisplayValue() {
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseByDisplayValue("Fake"));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.ARCHIVED.getDisplayValue()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.DEPRECATED.getDisplayValue()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.DEVELOPMENT.getDisplayValue()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.PLANNING.getDisplayValue()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.RELEASED.getDisplayValue()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.UNKNOWNPHASE.getDisplayValue()));
    }
}
