package com.blackducksoftware.integration.hub.response.mapping;

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
import com.blackducksoftware.integration.hub.scan.api.ScanLocationResults;
import com.blackducksoftware.integration.hub.scan.status.ScanStatus;

public class ScanLocationResultsTest {

    @Test
    public void testScanLocationResults() {
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

        final ScanLocationItem scanLocation1 = new ScanLocationItem();
        scanLocation1.setAssetReferenceList(assetList1);
        scanLocation1.setScanList(scanList1);
        scanLocation1.setHost("host1");
        scanLocation1.setId("id1");
        scanLocation1.setLastScanUploadDate("lastScanUploadDate1");
        scanLocation1.setPath("path1");
        scanLocation1.setScanId("scanId1");
        scanLocation1.setScanInitiatorName("scanInitiatorName1");
        scanLocation1.setScanTime("scanTime1");
        final ScanLocationItem scanLocation2 = new ScanLocationItem();
        scanLocation2.setAssetReferenceList(assetList2);
        scanLocation2.setScanList(scanList2);
        scanLocation2.setHost("host2");
        scanLocation2.setId("id2");
        scanLocation2.setLastScanUploadDate("lastScanUploadDate2");
        scanLocation2.setPath("path2");
        scanLocation2.setScanId("scanId2");
        scanLocation2.setScanInitiatorName("scanInitiatorName2");
        scanLocation2.setScanTime("scanTime2");

        final List<ScanLocationItem> scanLocationList1 = new ArrayList<ScanLocationItem>();
        scanLocationList1.add(scanLocation1);
        final List<ScanLocationItem> scanLocationList2 = new ArrayList<ScanLocationItem>();
        scanLocationList1.add(scanLocation2);

        final Integer totalCount1 = 1;
        final Integer totalCount2 = 2;

        ScanLocationResults item1 = new ScanLocationResults();
        item1.setItems(scanLocationList1);
        item1.setTotalCount(totalCount1);
        ScanLocationResults item2 = new ScanLocationResults();
        item2.setItems(scanLocationList2);
        item2.setTotalCount(totalCount2);
        ScanLocationResults item3 = new ScanLocationResults();
        item3.setItems(scanLocationList1);
        item3.setTotalCount(totalCount1);

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ScanLocationResults.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ScanLocationResults [totalCount=");
        builder.append(item1.getTotalCount());
        builder.append(", items=");
        builder.append(item1.getItems());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }
}
