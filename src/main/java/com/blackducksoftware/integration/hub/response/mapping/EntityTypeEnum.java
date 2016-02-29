package com.blackducksoftware.integration.hub.response.mapping;


public enum EntityTypeEnum {

    RL // Release? Used for owner Entity Type
    ,
    CL // Code Location? Used for the asset Entity Type
    ,
    UNKNOWNENTITY;

    public static EntityTypeEnum getEntityTypeEnum(String entityType) {
        EntityTypeEnum entityTypeEnum = UNKNOWNENTITY;
        try {
            entityTypeEnum = EntityTypeEnum.valueOf(entityType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ignore expection
        }
        return entityTypeEnum;
    }

}
