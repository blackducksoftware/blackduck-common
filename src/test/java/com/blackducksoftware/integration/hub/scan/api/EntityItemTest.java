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
package com.blackducksoftware.integration.hub.scan.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.scan.api.EntityItem;
import com.blackducksoftware.integration.hub.scan.api.EntityTypeEnum;

public class EntityItemTest {

    @Test
    public void testEntityItem() {
        final String entityId1 = "TestEntId1";
        final String entityType1 = EntityTypeEnum.RL.name();
        final String id1 = "TestId1";
        final String projectId1 = "ProjId1";
        final String projectName1 = "Proj1";
        final String releaseId1 = "ReleaseId1";
        final String release1 = "Release1";

        final String entityId2 = "TestEntId2";
        final String entityType2 = EntityTypeEnum.RL.name();
        final String id2 = "TestId2";
        final String projectId2 = "ProjId2";
        final String projectName2 = "Proj2";
        final String releaseId2 = "ReleaseId2";
        final String release2 = "Release2";

        EntityItem entity1 = new EntityItem();
        entity1.setEntityId(entityId1);
        entity1.setEntityType(entityType1);
        entity1.setId(id1);
        entity1.setProjectId(projectId1);
        entity1.setProjectName(projectName1);
        entity1.setReleaseId(releaseId1);
        entity1.setReleaseName(release1);
        EntityItem entity2 = new EntityItem();
        entity2.setEntityId(entityId2);
        entity2.setEntityType(entityType2);
        entity2.setId(id2);
        entity2.setProjectId(projectId2);
        entity2.setProjectName(projectName2);
        entity2.setReleaseId(releaseId2);
        entity2.setReleaseName(release2);
        EntityItem entity3 = new EntityItem();
        entity3.setEntityId(entityId1);
        entity3.setEntityType(entityType1);
        entity3.setId(id1);
        entity3.setProjectId(projectId1);
        entity3.setProjectName(projectName1);
        entity3.setReleaseId(releaseId1);
        entity3.setReleaseName(release1);

        assertEquals(entityId1, entity1.getEntityId());
        assertEquals(entityType1, entity1.getEntityType());
        assertEquals(id1, entity1.getId());
        assertEquals(projectId1, entity1.getProjectId());
        assertEquals(projectName1, entity1.getProjectName());
        assertEquals(releaseId1, entity1.getReleaseId());
        assertEquals(release1, entity1.getReleaseName());

        assertEquals(entityId2, entity2.getEntityId());
        assertEquals(entityType2, entity2.getEntityType());
        assertEquals(id2, entity2.getId());
        assertEquals(projectId2, entity2.getProjectId());
        assertEquals(projectName2, entity2.getProjectName());
        assertEquals(releaseId2, entity2.getReleaseId());
        assertEquals(release2, entity2.getReleaseName());

        assertTrue(!entity1.equals(entity2));
        assertTrue(entity1.equals(entity3));

        EqualsVerifier.forClass(EntityItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(entity1.hashCode() != entity2.hashCode());
        assertEquals(entity1.hashCode(), entity3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("EntityItem [entityType=");
        builder.append(entity1.getEntityType());
        builder.append(", entityId=");
        builder.append(entity1.getEntityId());
        builder.append(", id=");
        builder.append(entity1.getId());
        builder.append(", projectId=");
        builder.append(entity1.getProjectId());
        builder.append(", projectName=");
        builder.append(entity1.getProjectName());
        builder.append(", releaseId=");
        builder.append(entity1.getReleaseId());
        builder.append(", releaseName=");
        builder.append(entity1.getReleaseName());
        builder.append("]");

        assertEquals(builder.toString(), entity1.toString());
    }

}
