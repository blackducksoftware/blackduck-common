package com.blackducksoftware.integration.hub.response.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.scan.status.ScanStatus;

public class ScanStatusTest {

    @Test
    public void testGetScanStatus() {
        assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus("Fake"));
        assertEquals(ScanStatus.UNSTARTED, ScanStatus.getScanStatus(ScanStatus.UNSTARTED.toString().toLowerCase()));
        assertEquals(ScanStatus.UNSTARTED, ScanStatus.getScanStatus(ScanStatus.UNSTARTED.toString()));
        assertEquals(ScanStatus.SCANNING, ScanStatus.getScanStatus(ScanStatus.SCANNING.toString().toLowerCase()));
        assertEquals(ScanStatus.SCANNING, ScanStatus.getScanStatus(ScanStatus.SCANNING.toString()));
        assertEquals(ScanStatus.SAVING_SCAN_DATA, ScanStatus.getScanStatus(ScanStatus.SAVING_SCAN_DATA.toString().toLowerCase()));
        assertEquals(ScanStatus.SAVING_SCAN_DATA, ScanStatus.getScanStatus(ScanStatus.SAVING_SCAN_DATA.toString()));
        assertEquals(ScanStatus.SCAN_DATA_SAVE_COMPLETE, ScanStatus.getScanStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE.toString().toLowerCase()));
        assertEquals(ScanStatus.SCAN_DATA_SAVE_COMPLETE, ScanStatus.getScanStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE.toString()));
        assertEquals(ScanStatus.REQUESTED_MATCH_JOB, ScanStatus.getScanStatus(ScanStatus.REQUESTED_MATCH_JOB.toString().toLowerCase()));
        assertEquals(ScanStatus.REQUESTED_MATCH_JOB, ScanStatus.getScanStatus(ScanStatus.REQUESTED_MATCH_JOB.toString()));
        assertEquals(ScanStatus.MATCHING, ScanStatus.getScanStatus(ScanStatus.MATCHING.toString().toLowerCase()));
        assertEquals(ScanStatus.MATCHING, ScanStatus.getScanStatus(ScanStatus.MATCHING.toString()));
        assertEquals(ScanStatus.BOM_VERSION_CHECK, ScanStatus.getScanStatus(ScanStatus.BOM_VERSION_CHECK.toString().toLowerCase()));
        assertEquals(ScanStatus.BOM_VERSION_CHECK, ScanStatus.getScanStatus(ScanStatus.BOM_VERSION_CHECK.toString()));
        assertEquals(ScanStatus.BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.BUILDING_BOM.toString().toLowerCase()));
        assertEquals(ScanStatus.BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.BUILDING_BOM.toString()));
        assertEquals(ScanStatus.COMPLETE, ScanStatus.getScanStatus(ScanStatus.COMPLETE.toString().toLowerCase()));
        assertEquals(ScanStatus.COMPLETE, ScanStatus.getScanStatus(ScanStatus.COMPLETE.toString()));
        assertEquals(ScanStatus.CANCELLED, ScanStatus.getScanStatus(ScanStatus.CANCELLED.toString().toLowerCase()));
        assertEquals(ScanStatus.CANCELLED, ScanStatus.getScanStatus(ScanStatus.CANCELLED.toString()));
        assertEquals(ScanStatus.ERROR_SCANNING, ScanStatus.getScanStatus(ScanStatus.ERROR_SCANNING.toString().toLowerCase()));
        assertEquals(ScanStatus.ERROR_SCANNING, ScanStatus.getScanStatus(ScanStatus.ERROR_SCANNING.toString()));
        assertEquals(ScanStatus.ERROR_SAVING_SCAN_DATA, ScanStatus.getScanStatus(ScanStatus.ERROR_SAVING_SCAN_DATA.toString().toLowerCase()));
        assertEquals(ScanStatus.ERROR_SAVING_SCAN_DATA, ScanStatus.getScanStatus(ScanStatus.ERROR_SAVING_SCAN_DATA.toString()));
        assertEquals(ScanStatus.ERROR_MATCHING, ScanStatus.getScanStatus(ScanStatus.ERROR_MATCHING.toString().toLowerCase()));
        assertEquals(ScanStatus.ERROR_MATCHING, ScanStatus.getScanStatus(ScanStatus.ERROR_MATCHING.toString()));
        assertEquals(ScanStatus.ERROR_BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.ERROR_BUILDING_BOM.toString().toLowerCase()));
        assertEquals(ScanStatus.ERROR_BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.ERROR_BUILDING_BOM.toString()));
        assertEquals(ScanStatus.ERROR, ScanStatus.getScanStatus(ScanStatus.ERROR.toString().toLowerCase()));
        assertEquals(ScanStatus.ERROR, ScanStatus.getScanStatus(ScanStatus.ERROR.toString()));
        assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(ScanStatus.UNKNOWN.toString().toLowerCase()));
        assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(ScanStatus.UNKNOWN.toString()));
        assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(null));
    }

    @Test
    public void testIsFinishedStatus() {
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.BOM_VERSION_CHECK));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.BUILDING_BOM));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.CANCELLED));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.COMPLETE));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.ERROR));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.ERROR_BUILDING_BOM));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.ERROR_MATCHING));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.ERROR_SAVING_SCAN_DATA));
        assertTrue(ScanStatus.isFinishedStatus(ScanStatus.ERROR_SCANNING));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.MATCHING));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.REQUESTED_MATCH_JOB));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.SAVING_SCAN_DATA));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.SCANNING));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.UNKNOWN));
        assertTrue(!ScanStatus.isFinishedStatus(ScanStatus.UNSTARTED));
    }

    @Test
    public void testIsErrorStatus() {
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.BOM_VERSION_CHECK));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.BUILDING_BOM));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.CANCELLED));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.COMPLETE));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.ERROR));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.ERROR_BUILDING_BOM));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.ERROR_MATCHING));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.ERROR_SAVING_SCAN_DATA));
        assertTrue(ScanStatus.isErrorStatus(ScanStatus.ERROR_SCANNING));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.MATCHING));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.REQUESTED_MATCH_JOB));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.SAVING_SCAN_DATA));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.SCANNING));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.UNKNOWN));
        assertTrue(!ScanStatus.isErrorStatus(ScanStatus.UNSTARTED));

    }
}
