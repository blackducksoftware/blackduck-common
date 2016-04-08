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
package com.blackducksoftware.integration.hub.report.risk.api;

public class RiskCounts {

	private final int HIGH;

	private final int MEDIUM;

	private final int LOW;

	private final int OK;

	private final int UNKNOWN;

	public RiskCounts(
			final int HIGH,
			final int MEDIUM,
			final int LOW,
			final int OK,
			final int UNKNOWN) {
		this.HIGH = HIGH;
		this.MEDIUM = MEDIUM;
		this.LOW = LOW;
		this.OK = OK;
		this.UNKNOWN = UNKNOWN;
	}

	public int getHIGH() {
		return HIGH;
	}

	public int getMEDIUM() {
		return MEDIUM;
	}

	public int getLOW() {
		return LOW;
	}

	public int getOK() {
		return OK;
	}

	public int getUNKNOWN() {
		return UNKNOWN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + HIGH;
		result = prime * result + LOW;
		result = prime * result + MEDIUM;
		result = prime * result + OK;
		result = prime * result + UNKNOWN;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RiskCounts)) {
			return false;
		}
		final RiskCounts other = (RiskCounts) obj;
		if (HIGH != other.HIGH) {
			return false;
		}
		if (LOW != other.LOW) {
			return false;
		}
		if (MEDIUM != other.MEDIUM) {
			return false;
		}
		if (OK != other.OK) {
			return false;
		}
		if (UNKNOWN != other.UNKNOWN) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("RiskCounts [HIGH=");
		builder.append(HIGH);
		builder.append(", MEDIUM=");
		builder.append(MEDIUM);
		builder.append(", LOW=");
		builder.append(LOW);
		builder.append(", OK=");
		builder.append(OK);
		builder.append(", UNKNOWN=");
		builder.append(UNKNOWN);
		builder.append("]");
		return builder.toString();
	}

}
