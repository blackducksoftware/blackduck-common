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
package com.blackducksoftware.integration.hub.version.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.version.api.DistributionEnum;

public class DistributionEnumTest {

    @Test
    public void testGetDistributionEnum() {
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum("Fake"));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.EXTERNAL.toString().toLowerCase()));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.EXTERNAL.toString()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.INTERNAL.toString().toLowerCase()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionEnum(DistributionEnum.INTERNAL.toString()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionEnum(DistributionEnum.OPENSOURCE.toString().toLowerCase()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionEnum(DistributionEnum.OPENSOURCE.toString()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionEnum(DistributionEnum.SAAS.toString().toLowerCase()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionEnum(DistributionEnum.SAAS.toString()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(DistributionEnum.UNKNOWNDISTRIBUTION.toString().toLowerCase()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(DistributionEnum.UNKNOWNDISTRIBUTION.toString()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionEnum(null));
    }

    @Test
    public void testGetDistributionEnumByDisplayValue() {
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION, DistributionEnum.getDistributionByDisplayValue("Fake"));
        assertEquals(DistributionEnum.EXTERNAL, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.EXTERNAL.getDisplayValue()));
        assertEquals(DistributionEnum.INTERNAL, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.INTERNAL.getDisplayValue()));
        assertEquals(DistributionEnum.OPENSOURCE, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.OPENSOURCE.getDisplayValue()));
        assertEquals(DistributionEnum.SAAS, DistributionEnum.getDistributionByDisplayValue(DistributionEnum.SAAS.getDisplayValue()));
        assertEquals(DistributionEnum.UNKNOWNDISTRIBUTION,
                DistributionEnum.getDistributionByDisplayValue(DistributionEnum.UNKNOWNDISTRIBUTION.getDisplayValue()));
    }
}
