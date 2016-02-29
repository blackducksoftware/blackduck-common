package com.blackducksoftware.integration.hub.report.risk.api;

public class RiskCounts {

    private final int HIGH;

    private final int MEDIUM;

    private final int LOW;

    private final int OK;

    private final int UNKNOWN;

    public RiskCounts(
            int HIGH,
            int MEDIUM,
            int LOW,
            int OK,
            int UNKNOWN) {
        this.HIGH = HIGH;
        this.MEDIUM = MEDIUM;
        this.LOW = LOW;
        this.OK = OK;
        this.UNKNOWN = UNKNOWN;
    }

    public int getHIGH() {
        return HIGH;
    }

    public int getMEDIUM() {
        return MEDIUM;
    }

    public int getLOW() {
        return LOW;
    }

    public int getOK() {
        return OK;
    }

    public int getUNKNOWN() {
        return UNKNOWN;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + HIGH;
        result = prime * result + LOW;
        result = prime * result + MEDIUM;
        result = prime * result + OK;
        result = prime * result + UNKNOWN;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        RiskCounts other = (RiskCounts) obj;
        if (HIGH != other.HIGH) {
            return false;
        }
        if (LOW != other.LOW) {
            return false;
        }
        if (MEDIUM != other.MEDIUM) {
            return false;
        }
        if (OK != other.OK) {
            return false;
        }
        if (UNKNOWN != other.UNKNOWN) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RiskCategory [HIGH=");
        builder.append(HIGH);
        builder.append(", MEDIUM=");
        builder.append(MEDIUM);
        builder.append(", LOW=");
        builder.append(LOW);
        builder.append(", OK=");
        builder.append(OK);
        builder.append(", UNKNOWN=");
        builder.append(UNKNOWN);
        builder.append("]");
        return builder.toString();
    }

}
