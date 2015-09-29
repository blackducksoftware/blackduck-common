package com.blackducksoftware.integration.hub.response;

public enum PhaseEnum {
    PLANNING
    ,
    DEVELOPMENT
    ,
    RELEASED
    ,
    DEPRECATED
    ,
    ARCHIVED
    ,
    UNKNOWNPHASE;

    public static PhaseEnum getPhaseEnum(String phaseEnum) {
        if (phaseEnum.equalsIgnoreCase(PLANNING.name())) {
            return PhaseEnum.PLANNING;
        } else if (phaseEnum.equalsIgnoreCase(DEVELOPMENT.name())) {
            return PhaseEnum.DEVELOPMENT;
        } else if (phaseEnum.equalsIgnoreCase(RELEASED.name())) {
            return PhaseEnum.RELEASED;
        } else if (phaseEnum.equalsIgnoreCase(DEPRECATED.name())) {
            return PhaseEnum.DEPRECATED;
        } else if (phaseEnum.equalsIgnoreCase(ARCHIVED.name())) {
            return PhaseEnum.ARCHIVED;
        } else {
            return PhaseEnum.UNKNOWNPHASE;
        }
    }
}
