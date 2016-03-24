package com.blackducksoftware.integration.hub.scan.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.scan.api.AssetReferenceItem;
import com.blackducksoftware.integration.hub.scan.api.EntityItem;
import com.blackducksoftware.integration.hub.scan.api.EntityTypeEnum;
import com.blackducksoftware.integration.hub.scan.api.ScanHistoryItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.status.ScanStatus;

public class ScanLocationItemTest {

    @Test
    public void testScanHistoryItem() {
        final ScanHistoryItem scanHistory1 = new ScanHistoryItem();
        scanHistory1.setCreatedByUserName("User1");
        scanHistory1.setCreatedOn("Time1");
        scanHistory1.setLastModifiedOn("Modified1");
        scanHistory1.setNumDirs("NumDirs1");
        scanHistory1.setNumNonDirFiles("Files1");
        scanHistory1.setScannerVersion("Scanner1");
        scanHistory1.setScanSourceType("Source1");
        scanHistory1.setStatus(ScanStatus.BUILDING_BOM);
        final ScanHistoryItem scanHistory2 = new ScanHistoryItem();
        scanHistory2.setCreatedByUserName("User2");
        scanHistory2.setCreatedOn("Time2");
        scanHistory2.setLastModifiedOn("Modified2");
        scanHistory2.setNumDirs("NumDirs2");
        scanHistory2.setNumNonDirFiles("Files2");
        scanHistory2.setScannerVersion("Scanner2");
        scanHistory2.setScanSourceType("Source2");
        scanHistory2.setStatus(ScanStatus.COMPLETE);

        final List<ScanHistoryItem> scanList1 = new ArrayList<ScanHistoryItem>();
        scanList1.add(scanHistory1);
        final List<ScanHistoryItem> scanList2 = new ArrayList<ScanHistoryItem>();
        scanList2.add(scanHistory2);

        final EntityItem entity1 = new EntityItem();
        entity1.setEntityId("TestId1");
        entity1.setEntityType(EntityTypeEnum.RL.name());
        entity1.setId("TestId1");
        entity1.setProjectName("Proj1");
        final EntityItem entity2 = new EntityItem();
        entity2.setEntityId("TestId2");
        entity2.setEntityType(EntityTypeEnum.CL.name());
        entity2.setId("TestId2");
        entity2.setProjectName("Proj2");

        final AssetReferenceItem assertRef1 = new AssetReferenceItem();
        assertRef1.setAssetEntityKey(entity1);
        assertRef1.setOwnerEntityKey(entity1);
        final AssetReferenceItem assertRef2 = new AssetReferenceItem();
        assertRef2.setAssetEntityKey(entity2);
        assertRef2.setOwnerEntityKey(entity2);

        final List<AssetReferenceItem> assetList1 = new ArrayList<AssetReferenceItem>();
        assetList1.add(assertRef1);
        final List<AssetReferenceItem> assetList2 = new ArrayList<AssetReferenceItem>();
        assetList2.add(assertRef2);

        final String id1 = "Id1";
        final String scanId1 = "ScanId1";
        final String host1 = "Host1";
        final String path1 = "Path1";
        final String scanInitiatorName1 = "scanInitName1";
        final String lastScanUploadDate1 = "scanUpload1";
        final String scanTime1 = "ScanTime1";

        final String id2 = "Id2";
        final String scanId2 = "ScanId2";
        final String host2 = "Host2";
        final String path2 = "Path2";
        final String scanInitiatorName2 = "scanInitName2";
        final String lastScanUploadDate2 = "scanUpload2";
        final String scanTime2 = "ScanTime2";

        ScanLocationItem item1 = new ScanLocationItem();
        item1.setAssetReferenceList(assetList1);
        item1.setScanList(scanList1);
        item1.setHost(host1);
        item1.setId(id1);
        item1.setLastScanUploadDate(lastScanUploadDate1);
        item1.setPath(path1);
        item1.setScanId(scanId1);
        item1.setScanInitiatorName(scanInitiatorName1);
        item1.setScanTime(scanTime1);
        ScanLocationItem item2 = new ScanLocationItem();
        item2.setAssetReferenceList(assetList2);
        item2.setScanList(scanList2);
        item2.setHost(host2);
        item2.setId(id2);
        item2.setLastScanUploadDate(lastScanUploadDate2);
        item2.setPath(path2);
        item2.setScanId(scanId2);
        item2.setScanInitiatorName(scanInitiatorName2);
        item2.setScanTime(scanTime2);
        ScanLocationItem item3 = new ScanLocationItem();
        item3.setAssetReferenceList(assetList1);
        item3.setScanList(scanList1);
        item3.setHost(host1);
        item3.setId(id1);
        item3.setLastScanUploadDate(lastScanUploadDate1);
        item3.setPath(path1);
        item3.setScanId(scanId1);
        item3.setScanInitiatorName(scanInitiatorName1);
        item3.setScanTime(scanTime1);

        assertEquals(id1, item1.getId());
        assertEquals(host1, item1.getHost());
        assertEquals(assetList1, item1.getAssetReferenceList());
        assertEquals(scanList1, item1.getScanList());
        assertEquals(lastScanUploadDate1, item1.getLastScanUploadDate());
        assertEquals(scanId1, item1.getScanId());
        assertEquals(path1, item1.getPath());
        assertEquals(scanInitiatorName1, item1.getScanInitiatorName());
        assertEquals(scanTime1, item1.getScanTime());

        assertEquals(id2, item2.getId());
        assertEquals(host2, item2.getHost());
        assertEquals(assetList2, item2.getAssetReferenceList());
        assertEquals(scanList2, item2.getScanList());
        assertEquals(lastScanUploadDate2, item2.getLastScanUploadDate());
        assertEquals(scanId2, item2.getScanId());
        assertEquals(path2, item2.getPath());
        assertEquals(scanInitiatorName2, item2.getScanInitiatorName());
        assertEquals(scanTime2, item2.getScanTime());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ScanLocationItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ScanLocationItem [id=");
        builder.append(item1.getId());
        builder.append(", scanId=");
        builder.append(item1.getScanId());
        builder.append(", host=");
        builder.append(item1.getHost());
        builder.append(", path=");
        builder.append(item1.getPath());
        builder.append(", scanInitiatorName=");
        builder.append(item1.getScanInitiatorName());
        builder.append(", lastScanUploadDate=");
        builder.append(item1.getLastScanUploadDate());
        builder.append(", scanTime=");
        builder.append(item1.getScanTime());
        builder.append(", scanList=");
        builder.append(item1.getScanList());
        builder.append(", assetReferenceList=");
        builder.append(item1.getAssetReferenceList());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }
}
