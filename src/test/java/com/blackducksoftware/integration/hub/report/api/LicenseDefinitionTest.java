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

import org.junit.Test;

public class LicenseDefinitionTest {

    @Test
    public void testLicenseDefinition() {
        final String licenseId1 = "licId1";
        final String discoveredAs1 = "discAs1";
        final String name1 = "name1";
        final String spdxId1 = "spdx1";
        final String ownership1 = "owner1";
        final String codeSharing1 = "codeShare1";
        final String licenseDisplay1 = "licDisp1";

        final String licenseId2 = UUID.randomUUID().toString();
        final String discoveredAs2 = "discAs2";
        final String name2 = "name2";
        final String spdxId2 = "spdx2";
        final String ownership2 = "owner2";
        final String codeSharing2 = "codeShare2";
        final String licenseDisplay2 = "licDisp2";

        LicenseDefinition item1 = new LicenseDefinition(licenseId1, discoveredAs1, name1, spdxId1,
                ownership1, codeSharing1, licenseDisplay1);
        LicenseDefinition item2 = new LicenseDefinition(licenseId2, discoveredAs2, name2, spdxId2,
                ownership2, codeSharing2, licenseDisplay2);
        LicenseDefinition item3 = new LicenseDefinition(licenseId1, discoveredAs1, name1, spdxId1,
                ownership1, codeSharing1, licenseDisplay1);

        assertEquals(licenseId1, item1.getLicenseId());
        assertEquals(discoveredAs1, item1.getDiscoveredAs());
        assertEquals(name1, item1.getName());
        assertEquals(spdxId1, item1.getSpdxId());
        assertEquals(ownership1, item1.getOwnership());
        assertEquals(codeSharing1, item1.getCodeSharing());
        assertEquals(licenseDisplay1, item1.getLicenseDisplay());

        assertEquals(licenseId2, item2.getLicenseId());
        assertEquals(discoveredAs2, item2.getDiscoveredAs());
        assertEquals(name2, item2.getName());
        assertEquals(spdxId2, item2.getSpdxId());
        assertEquals(ownership2, item2.getOwnership());
        assertEquals(codeSharing2, item2.getCodeSharing());
        assertEquals(licenseDisplay2, item2.getLicenseDisplay());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(LicenseDefinition.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        assertNull(item1.getLicenseUUId());
        item1 = new LicenseDefinition(null, null, null, null,
                null, null, null);
        assertNull(item1.getLicenseUUId());
        assertEquals(licenseId2, item2.getLicenseUUId().toString());

        StringBuilder builder = new StringBuilder();
        builder.append("LicenseDefinition [licenseId=");
        builder.append(item1.getLicenseId());
        builder.append(", discoveredAs=");
        builder.append(item1.getDiscoveredAs());
        builder.append(", name=");
        builder.append(item1.getName());
        builder.append(", spdxId=");
        builder.append(item1.getSpdxId());
        builder.append(", ownership=");
        builder.append(item1.getOwnership());
        builder.append(", codeSharing=");
        builder.append(item1.getCodeSharing());
        builder.append(", licenseDisplay=");
        builder.append(item1.getLicenseDisplay());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
