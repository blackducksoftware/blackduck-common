package com.blackducksoftware.integration.hub.response.mapping;

public enum EntityTypeEnum {

    RL // Release? Used for owner Entity Type
    ,
    CL
    ,
    UNKNOWN; // Code Location? Used for the asset Entity Type

    public static EntityTypeEnum getEntityTypeEnum(String entityType) {
        if (entityType.equalsIgnoreCase(RL.toString())) {
            return EntityTypeEnum.RL;
        } else if (entityType.equalsIgnoreCase(CL.toString())) {
            return EntityTypeEnum.CL;
        } else {
            return EntityTypeEnum.UNKNOWN;
        }
    }

}
