package com.blackducksoftware.integration.hub;

import java.util.Arrays;
import java.util.List;

public enum BuilderType {

	MAVEN,

	GRADLE,

	ANT,

	UNKNOWN_BUILDER;

	public static BuilderType getBuilderType(final String builder) {
		BuilderType entityTypeEnum;
		try {
			entityTypeEnum = BuilderType.valueOf(builder.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			entityTypeEnum = UNKNOWN_BUILDER;
		} catch (final NullPointerException e) {
			// ignore expection
			entityTypeEnum = UNKNOWN_BUILDER;
		}
		return entityTypeEnum;
	}

	public static List<BuilderType> getBuilderTypes() {
		return Arrays.asList(BuilderType.values());
	}

}
