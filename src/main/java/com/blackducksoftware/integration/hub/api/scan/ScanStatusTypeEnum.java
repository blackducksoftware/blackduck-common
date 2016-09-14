package com.blackducksoftware.integration.hub.api.scan;

import java.util.EnumSet;
import java.util.Set;

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

	private static final Set<ScanStatusTypeEnum> PENDING_STATES = EnumSet.of(UNSTARTED, SCANNING, SAVING_SCAN_DATA,
			SCAN_DATA_SAVE_COMPLETE, REQUESTED_MATCH_JOB, MATCHING, BOM_VERSION_CHECK, BUILDING_BOM);
	private static final Set<ScanStatusTypeEnum> DONE_STATES = EnumSet.of(COMPLETE, CANCELLED, CLONED, ERROR_SCANNING,
			ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR);

	public boolean isPending() {
		return PENDING_STATES.contains(this);
	}

	public boolean isDone() {
		return DONE_STATES.contains(this);
	}

}
