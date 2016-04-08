/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
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
