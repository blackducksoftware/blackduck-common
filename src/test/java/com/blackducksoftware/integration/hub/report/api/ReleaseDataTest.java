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
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ReleaseDataTest {

    @Test
    public void testReleaseData() {
        final String id1 = "Id1";
        final String version1 = "version1";

        final String id2 = "Id2";
        final String version2 = "version2";

        ReleaseData item1 = new ReleaseData(id1, version1);
        ReleaseData item2 = new ReleaseData(id2, version2);
        ReleaseData item3 = new ReleaseData(id1, version1);

        assertEquals(id1, item1.getId());
        assertEquals(version1, item1.getVersion());

        assertEquals(id2, item2.getId());
        assertEquals(version2, item2.getVersion());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ReleaseData.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ReleaseData [id=");
        builder.append(item1.getId());
        builder.append(", version=");
        builder.append(item1.getVersion());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
