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

import com.blackducksoftware.integration.hub.version.api.DistributionEnum;

public class DistributionEnumTest {

    @Test
    public void testGetDistributionEnum() {
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum("Fake"));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.EXTERNAL.toString().toLowerCase()));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.EXTERNAL.toString()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.INTERNAL.toString().toLowerCase()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.INTERNAL.toString()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionEnum(DistributionEnum.OPENSOURCE.toString().toLowerCase()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionEnum(DistributionEnum.OPENSOURCE.toString()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionEnum(DistributionEnum.SAAS.toString().toLowerCase()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionEnum(DistributionEnum.SAAS.toString()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(DistributionEnum.UNKNOWNDISTRIBUTION.toString().toLowerCase()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(DistributionEnum.UNKNOWNDISTRIBUTION.toString()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(null));
    }

    @Test
    public void testGetDistributionEnumByDisplayValue() {
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionByDisplayValue("Fake"));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.EXTERNAL.getDisplayValue()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.INTERNAL.getDisplayValue()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.OPENSOURCE.getDisplayValue()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.SAAS.getDisplayValue()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION,
                DistributionEnum.getDistributionByDisplayValue(DistributionEnum.UNKNOWNDISTRIBUTION.getDisplayValue()));
    }
}
