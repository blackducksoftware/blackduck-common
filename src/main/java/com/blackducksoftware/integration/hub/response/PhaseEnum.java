package com.blackducksoftware.integration.hub.response;

public enum PhaseEnum {
    PLANNING("In Planning")
    ,
    DEVELOPMENT("In Developement")
    ,
    RELEASED("Released")
    ,
    DEPRECATED("Deprecated")
    ,
    ARCHIVED("Archived")
    ,
    UNKNOWNPHASE("Unknown Phase");

    private final String displayValue;

    private PhaseEnum(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static PhaseEnum getPhaseByDisplayValue(String displayValue) {
        if (displayValue.equalsIgnoreCase(PLANNING.getDisplayValue())) {
            return PhaseEnum.PLANNING;
        } else if (displayValue.equalsIgnoreCase(DEVELOPMENT.getDisplayValue())) {
            return PhaseEnum.DEVELOPMENT;
        } else if (displayValue.equalsIgnoreCase(RELEASED.getDisplayValue())) {
            return PhaseEnum.RELEASED;
        } else if (displayValue.equalsIgnoreCase(DEPRECATED.getDisplayValue())) {
            return PhaseEnum.DEPRECATED;
        } else if (displayValue.equalsIgnoreCase(ARCHIVED.getDisplayValue())) {
            return PhaseEnum.ARCHIVED;
        }
        return PhaseEnum.UNKNOWNPHASE;
    }

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
