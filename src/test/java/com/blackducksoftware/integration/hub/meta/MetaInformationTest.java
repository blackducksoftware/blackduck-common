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
package com.blackducksoftware.integration.hub.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MetaInformationTest {
    @Test
    public void conversionTest() {
        final String metaJson = "{'allow': ['GET'],'href': 'https://test/api/projects/aacfcbd6-3625-4f3b-ba93-d1da3047d186','links': [{'rel': 'versions','href': 'https://test/api/projects/aacfcbd6-3625-4f3b-ba93-d1da3047d186/versions'},{'rel': 'canonicalVersion','href': 'https://test/api/projects/aacfcbd6-3625-4f3b-ba93-d1da3047d186/versions/bdd15fe4-3728-4b1a-af5e-b8972b2699a5'}]}";
        final Gson gson = new GsonBuilder().create();

        final MetaInformation meta = gson.fromJson(metaJson, MetaInformation.class);
        assertNotNull(meta);
        assertNotNull(meta.getAllow());
        assertTrue(!meta.getAllow().isEmpty());
        assertNotNull(meta.getLinks());
        assertTrue(!meta.getLinks().isEmpty());
        assertNotNull(meta.getHref());

        assertEquals(MetaAllowEnum.GET, meta.getAllow().get(0));
        assertEquals("https://test/api/projects/aacfcbd6-3625-4f3b-ba93-d1da3047d186", meta.getHref());
    }

}
