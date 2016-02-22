package com.blackducksoftware.integration.hub.report.risk.api;

public enum RiskCategory {
    LICENSE,
    ACTIVITY,
    VULNERABILITY,
    VERSION,
    OPERATIONAL,
    UNKNOWN;

    public static RiskCategory getRiskCategory(String riskCategory) {
        if (riskCategory.equalsIgnoreCase(LICENSE.name())) {
            return RiskCategory.LICENSE;
        } else if (riskCategory.equalsIgnoreCase(ACTIVITY.name())) {
            return RiskCategory.ACTIVITY;
        } else if (riskCategory.equalsIgnoreCase(VULNERABILITY.name())) {
            return RiskCategory.VULNERABILITY;
        } else if (riskCategory.equalsIgnoreCase(VERSION.name())) {
            return RiskCategory.VERSION;
        } else if (riskCategory.equalsIgnoreCase(OPERATIONAL.name())) {
            return RiskCategory.OPERATIONAL;
        } else {
            return RiskCategory.UNKNOWN;
        }
    }

}
