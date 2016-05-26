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
package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class ComponentVersionStatusCountTest {

    @Test
    public void testComponentVersionStatusCount() {
        final String name1 = "name1";
        final int value1 = 324;

        final String name2 = PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.name();
        final int value2 = 0;

        ComponentVersionStatusCount item1 = new ComponentVersionStatusCount(name1, value1);
        ComponentVersionStatusCount item2 = new ComponentVersionStatusCount(name2, value2);
        ComponentVersionStatusCount item3 = new ComponentVersionStatusCount(name1, value1);

        assertEquals(name1, item1.getName());
        assertEquals(PolicyStatusEnum.UNKNOWN, item1.getPolicyStatusFromName());
        assertEquals(value1, item1.getValue());

        assertEquals(name2, item2.getName());
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, item2.getPolicyStatusFromName());
        assertEquals(value2, item2.getValue());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(ComponentVersionStatusCount.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("ComponentVersionStatusCount [name=");
        builder.append(name1);
        builder.append(", value=");
        builder.append(value1);
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
