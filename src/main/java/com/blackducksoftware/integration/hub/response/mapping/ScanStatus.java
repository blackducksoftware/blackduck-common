package com.blackducksoftware.integration.hub.response.mapping;

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

    public static ScanStatus getScanStatus(String scanStatus) {
        if (scanStatus.equalsIgnoreCase(UNSTARTED.name())) {
            return ScanStatus.UNSTARTED;
        } else if (scanStatus.equalsIgnoreCase(SCANNING.name())) {
            return ScanStatus.SCANNING;
        } else if (scanStatus.equalsIgnoreCase(SAVING_SCAN_DATA.name())) {
            return ScanStatus.SAVING_SCAN_DATA;
        } else if (scanStatus.equalsIgnoreCase(SCAN_DATA_SAVE_COMPLETE.name())) {
            return ScanStatus.SCAN_DATA_SAVE_COMPLETE;
        } else if (scanStatus.equalsIgnoreCase(REQUESTED_MATCH_JOB.name())) {
            return ScanStatus.REQUESTED_MATCH_JOB;
        } else if (scanStatus.equalsIgnoreCase(MATCHING.name())) {
            return ScanStatus.MATCHING;
        } else if (scanStatus.equalsIgnoreCase(BOM_VERSION_CHECK.name())) {
            return ScanStatus.BOM_VERSION_CHECK;
        } else if (scanStatus.equalsIgnoreCase(BUILDING_BOM.name())) {
            return ScanStatus.BUILDING_BOM;
        } else if (scanStatus.equalsIgnoreCase(COMPLETE.name())) {
            return ScanStatus.COMPLETE;
        } else if (scanStatus.equalsIgnoreCase(CANCELLED.name())) {
            return ScanStatus.CANCELLED;
        } else if (scanStatus.equalsIgnoreCase(ERROR_SCANNING.name())) {
            return ScanStatus.ERROR_SCANNING;
        } else if (scanStatus.equalsIgnoreCase(ERROR_SAVING_SCAN_DATA.name())) {
            return ScanStatus.ERROR_SAVING_SCAN_DATA;
        } else if (scanStatus.equalsIgnoreCase(ERROR_MATCHING.name())) {
            return ScanStatus.ERROR_MATCHING;
        } else if (scanStatus.equalsIgnoreCase(ERROR_BUILDING_BOM.name())) {
            return ScanStatus.ERROR_BUILDING_BOM;
        } else if (scanStatus.equalsIgnoreCase(ERROR.name())) {
            return ScanStatus.ERROR;
        } else {
            return ScanStatus.UNKNOWN;
        }
    }

    /**
     * Returns true if the status some sort of end status.
     * COMPLETE, CANCELLED, ERROR_SCANNING, ERROR_SAVING_SCAN_DATA, ERROR_MATCHING, ERROR_BUILDING_BOM, ERROR
     *
     * @param scanStatus
     * @return
     */
    public static boolean isFinishedStatus(ScanStatus scanStatus) {
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
     * @param scanStatus
     * @return
     */
    public static boolean isErrorStatus(ScanStatus scanStatus) {
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
