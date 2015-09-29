package com.blackducksoftware.integration.hub.response.mapping;

public enum EntityTypeEnum {

    RL // Release? Used for owner Entity Type
    ,
    CL // Code Location? Used for the asset Entity Type
    ,
    UNKNOWNENTITY;

    public static EntityTypeEnum getEntityTypeEnum(String entityType) {
        if (entityType.equalsIgnoreCase(RL.name())) {
            return EntityTypeEnum.RL;
        } else if (entityType.equalsIgnoreCase(CL.name())) {
            return EntityTypeEnum.CL;
        } else {
            return EntityTypeEnum.UNKNOWNENTITY;
        }
    }

}
