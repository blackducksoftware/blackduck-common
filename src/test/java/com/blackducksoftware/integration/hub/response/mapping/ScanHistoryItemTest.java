package com.blackducksoftware.integration.hub.response.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.joda.time.DateTime;
import org.junit.Test;

public class ScanHistoryItemTest {

    @Test
    public void testScanHistoryItem() {
        final DateTime createdOnTime1 = new DateTime();
        final DateTime modifiedOnTime1 = new DateTime();

        final String scannerVersion1 = "Scanner1";
        final String lastModifiedOn1 = modifiedOnTime1.toString();
        final String createdOn1 = createdOnTime1.toString();
        final String createdByUserName1 = "Scanner1";
        final ScanStatus status1 = ScanStatus.BUILDING_BOM;
        final String scanSourceType1 = "ScanSource1";
        final String numDirs1 = "numDirs1";
        final String numNonDirFiles1 = "numFiles1";

        final DateTime createdOnTime2 = new DateTime();
        final DateTime modifiedOnTime2 = new DateTime();

        final String scannerVersion2 = "Scanner2";
        final String lastModifiedOn2 = modifiedOnTime2.toString();
        final String createdOn2 = createdOnTime2.toString();
        final String createdByUserName2 = "Scanner2";
        final ScanStatus status2 = ScanStatus.COMPLETE;
        final String scanSourceType2 = "ScanSource2";
        final String numDirs2 = "numDirs2";
        final String numNonDirFiles2 = "numFiles2";

        ScanHistoryItem item1 = new ScanHistoryItem();
        item1.setCreatedByUserName(createdByUserName1);
        item1.setCreatedOn(createdOn1);
        item1.setLastModifiedOn(lastModifiedOn1);
        item1.setNumDirs(numDirs1);
        item1.setNumNonDirFiles(numNonDirFiles1);
        item1.setScannerVersion(scannerVersion1);
        item1.setScanSourceType(scanSourceType1);
        item1.setStatus(status1);
        ScanHistoryItem item2 = new ScanHistoryItem();
        item2.setCreatedByUserName(createdByUserName2);
        item2.setCreatedOn(createdOn2);
        item2.setLastModifiedOn(lastModifiedOn2);
        item2.setNumDirs(numDirs2);
        item2.setNumNonDirFiles(numNonDirFiles2);
        item2.setScannerVersion(scannerVersion2);
        item2.setScanSourceType(scanSourceType2);
        item2.setStatus(status2);
        ScanHistoryItem item3 = new ScanHistoryItem();
        item3.setCreatedByUserName(createdByUserName1);
        item3.setCreatedOn(createdOn1);
        item3.setLastModifiedOn(lastModifiedOn1);
        item3.setNumDirs(numDirs1);
        item3.setNumNonDirFiles(numNonDirFiles1);
        item3.setScannerVersion(scannerVersion1);
        item3.setScanSourceType(scanSourceType1);
        item3.setStatus(status1);

        assertEquals(scannerVersion1, item1.getScannerVersion());
        assertEquals(lastModifiedOn1, item1.getLastModifiedOn());
        assertEquals(createdOn1, item1.getCreatedOn());
        assertEquals(createdByUserName1, item1.getCreatedByUserName());
        assertEquals(status1, item1.getStatus());
        assertEquals(scanSourceType1, item1.getScanSourceType());
        assertEquals(numDirs1, item1.getNumDirs());
        assertEquals(numNonDirFiles1, item1.getNumNonDirFiles());

        assertEquals(scannerVersion2, item2.getScannerVersion());
        assertEquals(lastModifiedOn2, item2.getLastModifiedOn());
        assertEquals(createdOn2, item2.getCreatedOn());
        assertEquals(createdByUserName2, item2.getCreatedByUserName());
        assertEquals(status2, item2.getStatus());
        assertEquals(scanSourceType2, item2.getScanSourceType());
        assertEquals(numDirs2, item2.getNumDirs());
        assertEquals(numNonDirFiles2, item2.getNumNonDirFiles());

        assertEquals(createdOnTime1, item1.getCreatedOnTime());
        assertEquals(modifiedOnTime1, item1.getLastModifiedOnTime());

        assertEquals(createdOnTime2, item2.getCreatedOnTime());
        assertEquals(modifiedOnTime2, item2.getLastModifiedOnTime());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ScanHistoryItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ScanHistoryItem [scannerVersion=");
        builder.append(item1.getScannerVersion());
        builder.append(", lastModifiedOn=");
        builder.append(item1.getLastModifiedOn());
        builder.append(", createdOn=");
        builder.append(item1.getCreatedOn());
        builder.append(", createdByUserName=");
        builder.append(item1.getCreatedByUserName());
        builder.append(", status=");
        builder.append(item1.getStatus());
        builder.append(", scanSourceType=");
        builder.append(item1.getScanSourceType());
        builder.append(", numDirs=");
        builder.append(item1.getNumDirs());
        builder.append(", numNonDirFiles=");
        builder.append(item1.getNumNonDirFiles());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }
}
