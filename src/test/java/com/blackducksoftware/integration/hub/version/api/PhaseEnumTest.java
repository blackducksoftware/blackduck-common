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
