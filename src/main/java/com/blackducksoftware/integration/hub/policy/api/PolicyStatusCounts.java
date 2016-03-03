package com.blackducksoftware.integration.hub.policy.api;

public class PolicyStatusCounts {
    private final String IN_VIOLATION;

    private final String IN_VIOLATION_OVERRIDEN;

    private final String NOT_IN_VIOLATION;

    public PolicyStatusCounts(String iN_VIOLATION, String iN_VIOLATION_OVERRIDEN, String nOT_IN_VIOLATION) {
        IN_VIOLATION = iN_VIOLATION;
        IN_VIOLATION_OVERRIDEN = iN_VIOLATION_OVERRIDEN;
        NOT_IN_VIOLATION = nOT_IN_VIOLATION;
    }

    public String getIN_VIOLATION() {
        return IN_VIOLATION;
    }

    public String getIN_VIOLATION_OVERRIDEN() {
        return IN_VIOLATION_OVERRIDEN;
    }

    public String getNOT_IN_VIOLATION() {
        return NOT_IN_VIOLATION;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((IN_VIOLATION == null) ? 0 : IN_VIOLATION.hashCode());
        result = prime * result + ((IN_VIOLATION_OVERRIDEN == null) ? 0 : IN_VIOLATION_OVERRIDEN.hashCode());
        result = prime * result + ((NOT_IN_VIOLATION == null) ? 0 : NOT_IN_VIOLATION.hashCode());
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
        if (!(obj instanceof PolicyStatusCounts)) {
            return false;
        }
        PolicyStatusCounts other = (PolicyStatusCounts) obj;
        if (IN_VIOLATION == null) {
            if (other.IN_VIOLATION != null) {
                return false;
            }
        } else if (!IN_VIOLATION.equals(other.IN_VIOLATION)) {
            return false;
        }
        if (IN_VIOLATION_OVERRIDEN == null) {
            if (other.IN_VIOLATION_OVERRIDEN != null) {
                return false;
            }
        } else if (!IN_VIOLATION_OVERRIDEN.equals(other.IN_VIOLATION_OVERRIDEN)) {
            return false;
        }
        if (NOT_IN_VIOLATION == null) {
            if (other.NOT_IN_VIOLATION != null) {
                return false;
            }
        } else if (!NOT_IN_VIOLATION.equals(other.NOT_IN_VIOLATION)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PolicyStatusCounts [IN_VIOLATION=");
        builder.append(IN_VIOLATION);
        builder.append(", IN_VIOLATION_OVERRIDEN=");
        builder.append(IN_VIOLATION_OVERRIDEN);
        builder.append(", NOT_IN_VIOLATION=");
        builder.append(NOT_IN_VIOLATION);
        builder.append("]");
        return builder.toString();
    }

}
