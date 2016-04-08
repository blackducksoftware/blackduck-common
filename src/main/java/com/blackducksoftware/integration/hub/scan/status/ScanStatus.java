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
package com.blackducksoftware.integration.hub.scan.status;

public enum ScanStatus {
	UNSTARTED,
	SCANNING,
	SAVING_SCAN_DATA,
	SCAN_DATA_SAVE_COMPLETE,
	REQUESTED_MATCH_JOB,
	MATCHING,
	BOM_VERSION_CHECK,
	BUILDING_BOM,
	COMPLETE,
	CANCELLED,
	ERROR_SCANNING,
	ERROR_SAVING_SCAN_DATA,
	ERROR_MATCHING,
	ERROR_BUILDING_BOM,
	ERROR,
	UNKNOWN;

	public static ScanStatus getScanStatus(final String scanStatus) {
		if (scanStatus == null) {
			return ScanStatus.UNKNOWN;
		}
		ScanStatus scanStatusEnum;
		try {
			scanStatusEnum = ScanStatus.valueOf(scanStatus.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			scanStatusEnum = UNKNOWN;
		}
		return scanStatusEnum;
	}

	/**
	 * Returns true if the status some sort of end status.
	 * COMPLETE, CANCELLED, ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR
	 *
	 */
	public static boolean isFinishedStatus(final ScanStatus scanStatus) {
		if (scanStatus == COMPLETE) {
			return true;
		} else if (isErrorStatus(scanStatus)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the status some sort of end status.
	 * CANCELLED, ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR
	 *
	 */
	public static boolean isErrorStatus(final ScanStatus scanStatus) {
		if (scanStatus == CANCELLED) {
			return true;
		} else if (scanStatus == ERROR_SCANNING) {
			return true;
		} else if (scanStatus == ERROR_SAVING_SCAN_DATA) {
			return true;
		} else if (scanStatus == ERROR_MATCHING) {
			return true;
		} else if (scanStatus == ERROR_BUILDING_BOM) {
			return true;
		} else if (scanStatus == ERROR) {
			return true;
		} else {
			return false;
		}
	}

}
