package com.blackducksoftware.integration.hub.response;

public enum DistributionEnum {

    EXTERNAL("External")
    ,
    SAAS("SaaS")
    ,
    INTERNAL("Internal")
    ,
    OPENSOURCE("Open Source (Hub 2.3)")
    ,
    UNKNOWNDISTRIBUTION("Unknown Distribution");

    private final String displayValue;

    private DistributionEnum(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static DistributionEnum getDistributionByDisplayValue(String displayValue) {
        if (displayValue.equalsIgnoreCase(EXTERNAL.getDisplayValue())) {
            return DistributionEnum.EXTERNAL;
        } else if (displayValue.equalsIgnoreCase(SAAS.getDisplayValue())) {
            return DistributionEnum.SAAS;
        } else if (displayValue.equalsIgnoreCase(INTERNAL.getDisplayValue())) {
            return DistributionEnum.INTERNAL;
        } else if (displayValue.equalsIgnoreCase(OPENSOURCE.getDisplayValue())) {
            return DistributionEnum.OPENSOURCE;
        }
        return DistributionEnum.UNKNOWNDISTRIBUTION;
    }

    public static DistributionEnum getDistributionEnum(String distributionEnum) {
        if (distributionEnum.equalsIgnoreCase(EXTERNAL.name())) {
            return DistributionEnum.EXTERNAL;
        } else if (distributionEnum.equalsIgnoreCase(SAAS.name())) {
            return DistributionEnum.SAAS;
        } else if (distributionEnum.equalsIgnoreCase(INTERNAL.name())) {
            return DistributionEnum.INTERNAL;
        }
        return DistributionEnum.UNKNOWNDISTRIBUTION;
    }
}
