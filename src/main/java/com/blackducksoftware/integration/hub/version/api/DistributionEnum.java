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


public enum DistributionEnum {

	EXTERNAL("External")
	,
	SAAS("SaaS")
	,
	INTERNAL("Internal")
	,
	OPENSOURCE("Open Source")
	,
	UNKNOWNDISTRIBUTION("Unknown Distribution");

	private final String displayValue;

	private DistributionEnum(final String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public static DistributionEnum getDistributionByDisplayValue(final String displayValue) {
		for (final DistributionEnum currentEnum : DistributionEnum.values()) {
			if (currentEnum.getDisplayValue().equalsIgnoreCase(displayValue)) {
				return currentEnum;
			}
		}
		return DistributionEnum.UNKNOWNDISTRIBUTION;
	}

	public static DistributionEnum getDistributionEnum(final String distribution) {
		if (distribution == null) {
			return DistributionEnum.UNKNOWNDISTRIBUTION;
		}
		DistributionEnum distributionEnum;
		try {
			distributionEnum = DistributionEnum.valueOf(distribution.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			distributionEnum = UNKNOWNDISTRIBUTION;
		}
		return distributionEnum;
	}
}
