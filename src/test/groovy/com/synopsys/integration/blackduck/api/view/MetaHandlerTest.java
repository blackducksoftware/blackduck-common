/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.api.view;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.hub.api.core.HubView;
import com.synopsys.integration.hub.api.generated.view.ProjectView;
import com.synopsys.integration.log.IntBufferedLogger;

public class MetaHandlerTest {
    private static final Gson gson = new Gson();
    private IntBufferedLogger logger;

    public HubView getTestHubItem() {
        final String json = "{\"name\":\"CITestProject\",\"projectLevelAdjustments\":false,\"source\":\"CUSTOM\",\"_meta\":{\"allow\":[\"GET\",\"PUT\",\"DELETE\"],\"href\":\"http://hub-server.com/api/projects/acae7891-cabb-4186-87ff-d650abb10a38\",\"links\":[{\"rel\":\"canonicalVersion\",\"href\":\"http://hub-server.com/api/projects/acae7891-cabb-4186-87ff-d650abb10a38/versions/96497043-89f9-4ae7-8b5a-e9945e0a57cf\"},{\"rel\":\"canonicalVersion\",\"href\":\"http://DoodleDoodleDoo\"}]}}";
        final ProjectView hubItem = gson.fromJson(json, ProjectView.class);
        hubItem.json = json;
        return hubItem;
    }

    public MetaHandler getMetaHandler() {
        logger = new IntBufferedLogger();
        final MetaHandler metaService = new MetaHandler(logger);
        return metaService;
    }

    @Test
    public void testHasLink() throws Exception {
        final MetaHandler metaHandler = getMetaHandler();
        final HubView hubItem = getTestHubItem();

        assertFalse(metaHandler.hasLink(hubItem, "users"));
        assertTrue(metaHandler.hasLink(hubItem, "canonicalVersion"));
    }

    @Test
    public void testGetFirstLinkSafely() throws Exception {
        final MetaHandler metaHandler = getMetaHandler();
        final HubView hubItem = getTestHubItem();

        assertNull(metaHandler.getFirstLinkSafely(hubItem, "non-existent-link"));
    }

    @Test
    public void testGetFirstLink() throws Exception {
        final MetaHandler metaHandler = getMetaHandler();
        final HubView hubItem = getTestHubItem();

        try {
            metaHandler.getFirstLink(hubItem, "users");
        } catch (final HubIntegrationException e) {
            assertTrue(e.getMessage().contains("Could not find the link '" + "users" + "', these are the available links : '" + "canonicalVersion" + "'"));
        }
        final String linkValue = metaHandler.getFirstLink(hubItem, "canonicalVersion");
        assertTrue(linkValue.startsWith("http"));
    }

    @Test
    public void testGetLinks() throws Exception {
        final MetaHandler metaHandler = getMetaHandler();
        final HubView hubItem = getTestHubItem();

        try {
            metaHandler.getLinks(hubItem, "users");
            Assert.fail("Should have thrown an exception");
        } catch (final HubIntegrationException e) {
            assertTrue(e.getMessage().contains("Could not find the link '" + "users" + "', these are the available links : '" + "canonicalVersion" + "'"));
        }

        final List<String> links = metaHandler.getLinks(hubItem, "canonicalVersion");
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
