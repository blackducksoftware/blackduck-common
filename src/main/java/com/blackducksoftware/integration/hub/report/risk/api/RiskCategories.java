package com.blackducksoftware.integration.hub.report.risk.api;

public class RiskCategories {

    private final RiskCounts VULNERABILITY;

    private final RiskCounts ACTIVITY;

    private final RiskCounts VERSION;

    private final RiskCounts LICENSE;

    private final RiskCounts OPERATIONAL;

    public RiskCategories(
            RiskCounts VULNERABILITY,
            RiskCounts ACTIVITY,
            RiskCounts VERSION,
            RiskCounts LICENSE,
            RiskCounts OPERATIONAL) {
        this.VULNERABILITY = VULNERABILITY;
        this.ACTIVITY = ACTIVITY;
        this.VERSION = VERSION;
        this.LICENSE = LICENSE;
        this.OPERATIONAL = OPERATIONAL;
    }

    public RiskCounts getVULNERABILITY() {
        return VULNERABILITY;
    }

    public RiskCounts getACTIVITY() {
        return ACTIVITY;
    }

    public RiskCounts getVERSION() {
        return VERSION;
    }

    public RiskCounts getLICENSE() {
        return LICENSE;
    }

    public RiskCounts getOPERATIONAL() {
        return OPERATIONAL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ACTIVITY == null) ? 0 : ACTIVITY.hashCode());
        result = prime * result + ((LICENSE == null) ? 0 : LICENSE.hashCode());
        result = prime * result + ((OPERATIONAL == null) ? 0 : OPERATIONAL.hashCode());
        result = prime * result + ((VERSION == null) ? 0 : VERSION.hashCode());
        result = prime * result + ((VULNERABILITY == null) ? 0 : VULNERABILITY.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RiskCategories)) {
            return false;
        }
        RiskCategories other = (RiskCategories) obj;
        if (ACTIVITY == null) {
            if (other.ACTIVITY != null) {
                return false;
            }
        } else if (!ACTIVITY.equals(other.ACTIVITY)) {
            return false;
        }
        if (LICENSE == null) {
            if (other.LICENSE != null) {
                return false;
            }
        } else if (!LICENSE.equals(other.LICENSE)) {
            return false;
        }
        if (OPERATIONAL == null) {
            if (other.OPERATIONAL != null) {
                return false;
            }
        } else if (!OPERATIONAL.equals(other.OPERATIONAL)) {
            return false;
        }
        if (VERSION == null) {
            if (other.VERSION != null) {
                return false;
            }
        } else if (!VERSION.equals(other.VERSION)) {
            return false;
        }
        if (VULNERABILITY == null) {
            if (other.VULNERABILITY != null) {
                return false;
            }
        } else if (!VULNERABILITY.equals(other.VULNERABILITY)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RiskCategory [VULNERABILITY=");
        builder.append(VULNERABILITY);
        builder.append(", ACTIVITY=");
        builder.append(ACTIVITY);
        builder.append(", VERSION=");
        builder.append(VERSION);
        builder.append(", LICENSE=");
        builder.append(LICENSE);
        builder.append(", OPERATIONAL=");
        builder.append(OPERATIONAL);
        builder.append("]");
        return builder.toString();
    }

}
