/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PolicyStatusEnumTest {

    @Test
    public void testGetPolicyStatusEnum() {
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum("Fake"));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN,
                PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN.toString()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.NOT_IN_VIOLATION, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.NOT_IN_VIOLATION.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString().toLowerCase()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(PolicyStatusEnum.UNKNOWN.toString()));
        assertEquals(PolicyStatusEnum.UNKNOWN, PolicyStatusEnum.getPolicyStatusEnum(null));
    }

}
