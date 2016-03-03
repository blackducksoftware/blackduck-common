package com.blackducksoftware.integration.hub.response.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EntityTypeEnumTest {

    @Test
    public void testGetEntityTypeEnum() {
        assertEquals(EntityTypeEnum.UNKNOWNENTITY, EntityTypeEnum.getEntityTypeEnum("Fake"));
        assertEquals(EntityTypeEnum.CL, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.CL.toString().toLowerCase()));
        assertEquals(EntityTypeEnum.CL, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.CL.toString()));
        assertEquals(EntityTypeEnum.RL, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.RL.toString().toLowerCase()));
        assertEquals(EntityTypeEnum.RL, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.RL.toString()));
        assertEquals(EntityTypeEnum.UNKNOWNENTITY, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.UNKNOWNENTITY.toString().toLowerCase()));
        assertEquals(EntityTypeEnum.UNKNOWNENTITY, EntityTypeEnum.getEntityTypeEnum(EntityTypeEnum.UNKNOWNENTITY.toString()));
        assertEquals(EntityTypeEnum.UNKNOWNENTITY, EntityTypeEnum.getEntityTypeEnum(null));
    }

}
