/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
	 * Returns true if the status some sort of end status. COMPLETE, CANCELLED,
	 * ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING,
	 * ERROR_BUILDING_BOM, ERROR
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
	 * Returns true if the status some sort of end status. CANCELLED,
	 * ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING,
	 * ERROR_BUILDING_BOM, ERROR
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
