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
package com.blackducksoftware.integration.hub.report.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.joda.time.DateTime;
import org.junit.Test;

import com.blackducksoftware.integration.hub.report.api.DetailedReleaseSummary.URLProvider;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

public class DetailedReleaseSummaryTest {

    @Test
    public void testDetailedReleaseSummary() {

        final String projectId1 = "projId1";
        final String versionId1 = "versionId1";
        final String projectName1 = "Proj1";
        final String version1 = "Version1";
        final String versionComments1 = "VersCom1";
        final String nickname1 = "nickname1";
        final String releasedOn1 = "releasedOn1";
        final String phase1 = "phase1";
        final String distribution1 = "dist1";

        final String baseUrl1 = "baseurl1";
        final URLProvider uiUrlGenerator1 = new URLProvider(baseUrl1);

        final String projectId2 = UUID.randomUUID().toString();
        final String versionId2 = UUID.randomUUID().toString();
        final String projectName2 = "Proj2";
        final String version2 = "Version2";
        final String versionComments2 = "VersCom2";
        final String nickname2 = "nickname2";
        final String releasedOn2 = (new DateTime()).toString();
        final String phase2 = PhaseEnum.ARCHIVED.name();
        final String distribution2 = DistributionEnum.EXTERNAL.name();

        final String baseUrl2 = "baseurl2";
        final URLProvider uiUrlGenerator2 = new URLProvider(baseUrl2);

        DetailedReleaseSummary item1 = new DetailedReleaseSummary(projectId1, versionId1,
                projectName1, version1, versionComments1, nickname1, releasedOn1, phase1,
                distribution1, uiUrlGenerator1);
        DetailedReleaseSummary item2 = new DetailedReleaseSummary(projectId2, versionId2,
                projectName2, version2, versionComments2, nickname2, releasedOn2, phase2,
                distribution2, uiUrlGenerator2);
        DetailedReleaseSummary item3 = new DetailedReleaseSummary(projectId1, versionId1,
                projectName1, version1, versionComments1, nickname1, releasedOn1, phase1,
                distribution1, uiUrlGenerator1);
        DetailedReleaseSummary item4 = new DetailedReleaseSummary(null, null, null, null, null, null, null, null, null, null);

        assertEquals(projectId1, item1.getProjectId());
        assertEquals(versionId1, item1.getVersionId());
        assertEquals(projectName1, item1.getProjectName());
        assertEquals(version1, item1.getVersion());
        assertEquals(versionComments1, item1.getVersionComments());
        assertEquals(nickname1, item1.getNickname());
        assertEquals(releasedOn1, item1.getReleasedOn());
        assertEquals(phase1, item1.getPhase());
        assertEquals(distribution1, item1.getDistribution());
        assertEquals(baseUrl1, uiUrlGenerator1.getBaseUrl());

        assertEquals(projectId2, item2.getProjectId());
        assertEquals(versionId2, item2.getVersionId());
        assertEquals(projectName2, item2.getProjectName());
        assertEquals(version2, item2.getVersion());
        assertEquals(versionComments2, item2.getVersionComments());
        assertEquals(nickname2, item2.getNickname());
        assertEquals(releasedOn2, item2.getReleasedOn());
        assertEquals(phase2, item2.getPhase());
        assertEquals(distribution2, item2.getDistribution());

        assertEquals(PhaseEnum.UNKNOWNPHASE.getDisplayValue(), item1.getPhaseDisplayValue());
        assertNull(item4.getPhaseDisplayValue());
        assertEquals(PhaseEnum.ARCHIVED.getDisplayValue(), item2.getPhaseDisplayValue());

        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION.getDisplayValue(), item1.getDistributionDisplayValue());
        assertNull(item4.getDistributionDisplayValue());
        assertEquals(DistributionEnum.EXTERNAL.getDisplayValue(), item2.getDistributionDisplayValue());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(DetailedReleaseSummary.class).suppress(Warning.STRICT_INHERITANCE).verify();
        EqualsVerifier.forClass(URLProvider.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        assertNull(item1.getProjectUUId());
        assertNull(item4.getProjectUUId());
        assertEquals(projectId2, item2.getProjectUUId().toString());
        assertNull(item1.getVersionUUId());
        assertNull(item4.getVersionUUId());
        assertEquals(versionId2, item2.getVersionUUId().toString());

        assertNull(item1.getReleasedOnTime());
        assertNull(item4.getReleasedOnTime());
        assertEquals(releasedOn2, item2.getReleasedOnTime().toString());

        StringBuilder builder = new StringBuilder();
        builder.append("DetailedReleaseSummary [projectId=");
        builder.append(item1.getProjectId());
        builder.append(", versionId=");
        builder.append(item1.getVersionId());
        builder.append(", projectName=");
        builder.append(item1.getProjectName());
        builder.append(", version=");
        builder.append(item1.getVersion());
        builder.append(", versionComments=");
        builder.append(item1.getVersionComments());
        builder.append(", nickname=");
        builder.append(item1.getNickname());
        builder.append(", releasedOn=");
        builder.append(item1.getReleasedOn());
        builder.append(", phase=");
        builder.append(item1.getPhase());
        builder.append(", distribution=");
        builder.append(item1.getDistribution());
        builder.append(", uiUrlGenerator=");
        builder.append(item1.getUiUrlGenerator());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }
}
