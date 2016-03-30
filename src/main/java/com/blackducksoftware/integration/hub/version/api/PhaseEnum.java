package com.blackducksoftware.integration.hub.version.api;

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

	private PhaseEnum(final String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public static PhaseEnum getPhaseByDisplayValue(final String displayValue) {
		for (final PhaseEnum currentEnum : PhaseEnum.values()) {
			if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
				return currentEnum;
			}
		}
		return PhaseEnum.UNKNOWNPHASE;
	}

	public static PhaseEnum getPhaseEnum(final String phase) {
		if (phase == null) {
			return PhaseEnum.UNKNOWNPHASE;
		}
		PhaseEnum phaseEnum;
		try {
			phaseEnum = PhaseEnum.valueOf(phase.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			phaseEnum = UNKNOWNPHASE;
		}
		return phaseEnum;
	}
}
