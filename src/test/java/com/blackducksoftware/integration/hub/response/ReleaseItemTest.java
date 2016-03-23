package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.version.api.ReleaseItem;

public class ReleaseItemTest {

    @Test
    public void testReleaseItem() {
        final String id1 = "ID1";
        final Boolean kb1 = true;
        final Boolean ohloh1 = true;
        final String projectId1 = "Project1";
        final String version1 = "Version1";
        final String phase1 = "Phase1";
        final String distribution1 = "Dist1";
        final String fileBomCodeLocationsUrl1 = "fileBomCL1";
        final String fileBomEntriesUrl1 = "fileBomEntries1";
        final String codeLocationsUrl1 = "codeLoc1";
        final String bomCountsUrl1 = "bomCount1";
        final String vulnerabilityCountsUrl1 = "vulnCount1";
        final String riskProfileUrl1 = "riskProf1";

        final String id2 = "ID2";
        final Boolean kb2 = false;
        final Boolean ohloh2 = false;
        final String projectId2 = "Project2";
        final String version2 = "Version2";
        final String phase2 = "Phase2";
        final String distribution2 = "Dist2";
        final String fileBomCodeLocationsUrl2 = "fileBomCL2";
        final String fileBomEntriesUrl2 = "fileBomEntries2";
        final String codeLocationsUrl2 = "codeLoc2";
        final String bomCountsUrl2 = "bomCount2";
        final String vulnerabilityCountsUrl2 = "vulnCount2";
        final String riskProfileUrl2 = "riskProf2";

        ReleaseItem item1 = new ReleaseItem(id1, kb1, ohloh1, projectId1, version1, phase1, distribution1,
                fileBomCodeLocationsUrl1, fileBomEntriesUrl1, codeLocationsUrl1, bomCountsUrl1, vulnerabilityCountsUrl1, riskProfileUrl1);
        ReleaseItem item2 = new ReleaseItem(id2, kb2, ohloh2, projectId2, version2, phase2, distribution2,
                fileBomCodeLocationsUrl2, fileBomEntriesUrl2, codeLocationsUrl2, bomCountsUrl2, vulnerabilityCountsUrl2, riskProfileUrl2);
        ReleaseItem item3 = new ReleaseItem(id1, kb1, ohloh1, projectId1, version1, phase1, distribution1,
                fileBomCodeLocationsUrl1, fileBomEntriesUrl1, codeLocationsUrl1, bomCountsUrl1, vulnerabilityCountsUrl1, riskProfileUrl1);

        ReleaseItem item4 = new ReleaseItem();
        item4.setId(id1);
        item4.setKb(kb1);
        item4.setOhloh(ohloh1);
        item4.setProjectId(projectId1);
        item4.setVersion(version1);
        item4.setPhase(phase1);
        item4.setDistribution(distribution1);
        item4.setFileBomCodeLocationsUrl(fileBomCodeLocationsUrl1);
        item4.setFileBomEntriesUrl(fileBomEntriesUrl1);
        item4.setCodeLocationsUrl(codeLocationsUrl1);
        item4.setBomCountsUrl(bomCountsUrl1);
        item4.setVulnerabilityCountsUrl(vulnerabilityCountsUrl1);
        item4.setRiskProfileUrl(riskProfileUrl1);

        assertEquals(id1, item4.getId());
        assertEquals(kb1, item4.getKb());
        assertEquals(ohloh1, item4.getOhloh());
        assertEquals(projectId1, item4.getProjectId());
        assertEquals(version1, item4.getVersion());
        assertEquals(phase1, item4.getPhase());
        assertEquals(distribution1, item4.getDistribution());
        assertEquals(fileBomCodeLocationsUrl1, item4.getFileBomCodeLocationsUrl());
        assertEquals(fileBomEntriesUrl1, item4.getFileBomEntriesUrl());
        assertEquals(codeLocationsUrl1, item4.getCodeLocationsUrl());
        assertEquals(bomCountsUrl1, item4.getBomCountsUrl());
        assertEquals(vulnerabilityCountsUrl1, item4.getVulnerabilityCountsUrl());
        assertEquals(riskProfileUrl1, item4.getRiskProfileUrl());

        assertEquals(id1, item1.getId());
        assertEquals(kb1, item1.getKb());
        assertEquals(ohloh1, item1.getOhloh());
        assertEquals(projectId1, item1.getProjectId());
        assertEquals(version1, item1.getVersion());
        assertEquals(phase1, item1.getPhase());
        assertEquals(distribution1, item1.getDistribution());
        assertEquals(fileBomCodeLocationsUrl1, item1.getFileBomCodeLocationsUrl());
        assertEquals(fileBomEntriesUrl1, item1.getFileBomEntriesUrl());
        assertEquals(codeLocationsUrl1, item1.getCodeLocationsUrl());
        assertEquals(bomCountsUrl1, item1.getBomCountsUrl());
        assertEquals(vulnerabilityCountsUrl1, item1.getVulnerabilityCountsUrl());
        assertEquals(riskProfileUrl1, item1.getRiskProfileUrl());

        assertEquals(id2, item2.getId());
        assertEquals(kb2, item2.getKb());
        assertEquals(ohloh2, item2.getOhloh());
        assertEquals(projectId2, item2.getProjectId());
        assertEquals(version2, item2.getVersion());
        assertEquals(phase2, item2.getPhase());
        assertEquals(distribution2, item2.getDistribution());
        assertEquals(fileBomCodeLocationsUrl2, item2.getFileBomCodeLocationsUrl());
        assertEquals(fileBomEntriesUrl2, item2.getFileBomEntriesUrl());
        assertEquals(codeLocationsUrl2, item2.getCodeLocationsUrl());
        assertEquals(bomCountsUrl2, item2.getBomCountsUrl());
        assertEquals(vulnerabilityCountsUrl2, item2.getVulnerabilityCountsUrl());
        assertEquals(riskProfileUrl2, item2.getRiskProfileUrl());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ReleaseItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ReleaseItem [id=");
        builder.append(item1.getId());
        builder.append(", kb=");
        builder.append(item1.getKb());
        builder.append(", ohloh=");
        builder.append(item1.getOhloh());
        builder.append(", projectId=");
        builder.append(item1.getProjectId());
        builder.append(", version=");
        builder.append(item1.getVersion());
        builder.append(", phase=");
        builder.append(item1.getPhase());
        builder.append(", distribution=");
        builder.append(item1.getDistribution());
        builder.append(", fileBomCodeLocationsUrl=");
        builder.append(item1.getFileBomCodeLocationsUrl());
        builder.append(", fileBomEntriesUrl=");
        builder.append(item1.getFileBomEntriesUrl());
        builder.append(", codeLocationsUrl=");
        builder.append(item1.getCodeLocationsUrl());
        builder.append(", bomCountsUrl=");
        builder.append(item1.getBomCountsUrl());
        builder.append(", vulnerabilityCountsUrl=");
        builder.append(item1.getVulnerabilityCountsUrl());
        builder.append(", riskProfileUrl=");
        builder.append(item1.getRiskProfileUrl());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
