package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PolicyStatusEnumTest {

    @Test
    public void testGetPolicyStatusEnum() {
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum("Fake"));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDEN,
                PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDEN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDEN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDEN.toString()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(null));
    }

}
