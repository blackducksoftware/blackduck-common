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

import com.blackducksoftware.integration.hub.scan.api.AssetReferenceItem;
import com.blackducksoftware.integration.hub.scan.api.EntityItem;
import com.blackducksoftware.integration.hub.scan.api.EntityTypeEnum;

public class AssetReferenceItemTest {

    @Test
    public void testAssetReferenceItem() {
        final EntityItem ownerEntity1 = new EntityItem();
        ownerEntity1.setEntityId("TestId1");
        ownerEntity1.setEntityType(EntityTypeEnum.RL.name());
        ownerEntity1.setId("TestId1");
        ownerEntity1.setProjectName("Proj1");
        final EntityItem assetEntity1 = new EntityItem();
        assetEntity1.setEntityId("TestId2");
        assetEntity1.setEntityType(EntityTypeEnum.CL.name());
        assetEntity1.setId("TestId2");
        assetEntity1.setProjectName("Proj2");
        final EntityItem ownerEntity2 = new EntityItem();
        ownerEntity2.setEntityId("TestId3");
        ownerEntity2.setEntityType(EntityTypeEnum.RL.name());
        ownerEntity2.setId("TestId3");
        ownerEntity2.setProjectName("Proj3");
        final EntityItem assetEntity2 = new EntityItem();
        assetEntity2.setEntityId("TestId4");
        assetEntity2.setEntityType(EntityTypeEnum.CL.name());
        assetEntity2.setId("TestId4");
        assetEntity2.setProjectName("Proj4");

        AssetReferenceItem item1 = new AssetReferenceItem();
        item1.setAssetEntityKey(assetEntity1);
        item1.setOwnerEntityKey(ownerEntity1);
        AssetReferenceItem item2 = new AssetReferenceItem();
        item2.setAssetEntityKey(assetEntity2);
        item2.setOwnerEntityKey(ownerEntity2);
        AssetReferenceItem item3 = new AssetReferenceItem();
        item3.setAssetEntityKey(assetEntity1);
        item3.setOwnerEntityKey(ownerEntity1);

        assertEquals(ownerEntity1, item1.getOwnerEntityKey());
        assertEquals(assetEntity1, item1.getAssetEntityKey());
        assertEquals(ownerEntity2, item2.getOwnerEntityKey());
        assertEquals(assetEntity2, item2.getAssetEntityKey());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(AssetReferenceItem.class).suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("AssetReferenceItem [ownerEntityKey=");
        builder.append(item1.getOwnerEntityKey());
        builder.append(", assetEntityKey=");
        builder.append(item1.getAssetEntityKey());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
