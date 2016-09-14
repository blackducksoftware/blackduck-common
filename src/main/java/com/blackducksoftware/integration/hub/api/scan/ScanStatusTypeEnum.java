package com.blackducksoftware.integration.hub.api.scan;

import java.util.Arrays;
import java.util.List;

public enum ScanStatusTypeEnum {
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
	CLONED,
	ERROR_SCANNING,
	ERROR_SAVING_SCAN_DATA,
	ERROR_MATCHING,
	ERROR_BUILDING_BOM,
	ERROR;

	private static final List<ScanStatusTypeEnum> PENDING_STATES = Arrays.asList(UNSTARTED, SCANNING, SAVING_SCAN_DATA,
			SCAN_DATA_SAVE_COMPLETE, REQUESTED_MATCH_JOB, MATCHING, BOM_VERSION_CHECK, BUILDING_BOM);
	private static final List<ScanStatusTypeEnum> DONE_STATES = Arrays.asList(COMPLETE, CANCELLED, CLONED,
			ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR);

	public boolean isPending() {
		return PENDING_STATES.contains(this);
	}

	public boolean isDone() {
		return DONE_STATES.contains(this);
	}

}
