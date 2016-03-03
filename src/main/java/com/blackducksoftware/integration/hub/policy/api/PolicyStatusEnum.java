package com.blackducksoftware.integration.hub.policy.api;

public enum PolicyStatusEnum {
    IN_VIOLATION,
    IN_VIOLATION_OVERRIDEN,
    NOT_IN_VIOLATION,
    UNKNOWN;

    public static PolicyStatusEnum getPolicyStatusEnum(String policyStatus) {
        PolicyStatusEnum policyStatusEnum;
        try {
            policyStatusEnum = PolicyStatusEnum.valueOf(policyStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ignore expection
            policyStatusEnum = UNKNOWN;
        } catch (NullPointerException e) {
            // ignore expection
            policyStatusEnum = UNKNOWN;
        }
        return policyStatusEnum;
    }
}
