/**
 * Hub Common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.api.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.google.gson.Gson;

public class MetaServiceTest {
    private static final Gson gson = new Gson();

    private IntBufferedLogger logger;

    public HubView getTestHubItem() {
        final String json = "{\"name\":\"CITestProject\",\"projectLevelAdjustments\":false,\"source\":\"CUSTOM\",\"_meta\":{\"allow\":[\"GET\",\"PUT\",\"DELETE\"],\"href\":\"http://hub-server.com/api/projects/acae7891-cabb-4186-87ff-d650abb10a38\",\"links\":[{\"rel\":\"canonicalVersion\",\"href\":\"http://hub-server.com/api/projects/acae7891-cabb-4186-87ff-d650abb10a38/versions/96497043-89f9-4ae7-8b5a-e9945e0a57cf\"},{\"rel\":\"canonicalVersion\",\"href\":\"http://DoodleDoodleDoo\"}]}}";
        final ProjectView hubItem = gson.fromJson(json, ProjectView.class);
        hubItem.json = json;
        return hubItem;
    }

    public MetaService getMetaService() {
        logger = new IntBufferedLogger();
        final MetaService metaService = new MetaService(logger);
        return metaService;
    }

    @Test
    public void testHasLink() throws Exception {
        final MetaService metaService = getMetaService();
        final HubView hubItem = getTestHubItem();

        assertFalse(metaService.hasLink(hubItem, MetaService.USERS_LINK));
        assertTrue(metaService.hasLink(hubItem, MetaService.CANONICAL_VERSION_LINK));
    }

    @Test
    public void testGetFirstLinkSafely() throws Exception {
        final MetaService metaService = getMetaService();
        final HubView hubItem = getTestHubItem();

        assertNull(metaService.getFirstLinkSafely(hubItem, "non-existent-link"));
    }

    @Test
    public void testGetFirstLink() throws Exception {
        final MetaService metaService = getMetaService();
        final HubView hubItem = getTestHubItem();

        try {
            metaService.getFirstLink(hubItem, MetaService.USERS_LINK);
        } catch (final HubIntegrationException e) {
            assertTrue(e.getMessage().contains("Could not find the link '" + MetaService.USERS_LINK + "', these are the available links : '" + MetaService.CANONICAL_VERSION_LINK + "'"));
        }
        final String linkValue = metaService.getFirstLink(hubItem, MetaService.CANONICAL_VERSION_LINK);
        assertTrue(linkValue.startsWith("http"));
    }

    @Test
    public void testGetLinks() throws Exception {
        final MetaService metaService = getMetaService();
        final HubView hubItem = getTestHubItem();

        try {
            metaService.getLinks(hubItem, MetaService.USERS_LINK);
            Assert.fail("Should have thrown an exception");
        } catch (final HubIntegrationException e) {
            assertTrue(e.getMessage().contains("Could not find the link '" + MetaService.USERS_LINK + "', these are the available links : '" + MetaService.CANONICAL_VERSION_LINK + "'"));
        }

        final List<String> links = metaService.getLinks(hubItem, MetaService.CANONICAL_VERSION_LINK);
        assertNotNull(links);
        assertTrue(!links.isEmpty());
        int nonHttpLinkCount = 0;
        for (final String l : links) {
            if (!l.startsWith("http")) {
                nonHttpLinkCount++;
            }
        }
        assertEquals(0, nonHttpLinkCount);
    }

}
