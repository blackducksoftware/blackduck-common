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
