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

import org.junit.Test;

public class PolicyStatusEnumTest {

    @Test
    public void testGetPolicyStatusEnum() {
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum("Fake"));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN,
                PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.toString()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(null));
    }

}
