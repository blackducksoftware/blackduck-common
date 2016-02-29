package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhaseEnumTest {

    @Test
    public void testGetPhaseEnum() {
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum("Fake"));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseEnum(PhaseEnum.ARCHIVED.toString().toLowerCase()));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseEnum(PhaseEnum.ARCHIVED.toString()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseEnum(PhaseEnum.DEPRECATED.toString().toLowerCase()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseEnum(PhaseEnum.DEPRECATED.toString()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseEnum(PhaseEnum.DEVELOPMENT.toString().toLowerCase()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseEnum(PhaseEnum.DEVELOPMENT.toString()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseEnum(PhaseEnum.PLANNING.toString().toLowerCase()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseEnum(PhaseEnum.PLANNING.toString()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseEnum(PhaseEnum.RELEASED.toString().toLowerCase()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseEnum(PhaseEnum.RELEASED.toString()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum(PhaseEnum.UNKNOWNPHASE.toString().toLowerCase()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseEnum(PhaseEnum.UNKNOWNPHASE.toString()));
    }

    @Test
    public void testGetPhaseEnumByDisplayValue() {
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseByDisplayValue("Fake"));
        assertEquals(PhaseEnum.ARCHIVED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.ARCHIVED.getDisplayValue()));
        assertEquals(PhaseEnum.DEPRECATED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.DEPRECATED.getDisplayValue()));
        assertEquals(PhaseEnum.DEVELOPMENT, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.DEVELOPMENT.getDisplayValue()));
        assertEquals(PhaseEnum.PLANNING, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.PLANNING.getDisplayValue()));
        assertEquals(PhaseEnum.RELEASED, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.RELEASED.getDisplayValue()));
        assertEquals(PhaseEnum.UNKNOWNPHASE, PhaseEnum.getPhaseByDisplayValue(PhaseEnum.UNKNOWNPHASE.getDisplayValue()));
    }
}
