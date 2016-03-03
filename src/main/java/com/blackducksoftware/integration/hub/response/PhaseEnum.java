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
        for (PhaseEnum currentEnum : PhaseEnum.values()) {
            if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return currentEnum;
            }
        }
        return PhaseEnum.UNKNOWNPHASE;
    }

    public static PhaseEnum getPhaseEnum(String phase) {
        PhaseEnum phaseEnum;
        try {
            phaseEnum = PhaseEnum.valueOf(phase.toUpperCase());
        } catch (IllegalArgumentException e) {
            // ignore expection
            phaseEnum = UNKNOWNPHASE;
        } catch (NullPointerException e) {
            // ignore expection
            phaseEnum = UNKNOWNPHASE;
        }
        return phaseEnum;
    }
}
