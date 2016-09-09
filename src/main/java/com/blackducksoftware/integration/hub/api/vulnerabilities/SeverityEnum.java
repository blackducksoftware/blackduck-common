package com.blackducksoftware.integration.hub.api.vulnerabilities;

public enum SeverityEnum {
	UNKNOWN, LOW, MEDIUM, HIGH;

	public static SeverityEnum getSeverityEnum(final String severity) {
		if (severity == null) {
			return SeverityEnum.UNKNOWN;
		}
		SeverityEnum severityEnum;
		try {
			severityEnum = SeverityEnum.valueOf(severity.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore
			severityEnum = UNKNOWN;
		}
		return severityEnum;
	}
}
