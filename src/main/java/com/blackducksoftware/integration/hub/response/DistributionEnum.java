package com.blackducksoftware.integration.hub.response;

public enum DistributionEnum {

    EXTERNAL
    ,
    SAAS
    ,
    INTERNAL
    ,
    UNKNOWNDISTRIBUTION;

    public static DistributionEnum getDistributionEnum(String distributionEnum) {
        if (distributionEnum.equalsIgnoreCase(EXTERNAL.name())) {
            return DistributionEnum.EXTERNAL;
        } else if (distributionEnum.equalsIgnoreCase(SAAS.name())) {
            return DistributionEnum.SAAS;
        } else if (distributionEnum.equalsIgnoreCase(INTERNAL.name())) {
            return DistributionEnum.INTERNAL;
        } else {
            return DistributionEnum.UNKNOWNDISTRIBUTION;
        }
    }
}
