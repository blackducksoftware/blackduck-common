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
