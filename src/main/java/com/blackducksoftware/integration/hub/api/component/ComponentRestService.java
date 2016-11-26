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
package com.blackducksoftware.integration.hub.api.component;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_COMPONENTS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubPagedRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.reflect.TypeToken;

public class ComponentRestService extends HubItemRestService<ComponentItem> {
    private static final List<String> COMPONENT_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_COMPONENTS);

    private static Type ITEM_TYPE = new TypeToken<ComponentItem>() {
    }.getType();

    private static Type ITEM_LIST_TYPE = new TypeToken<List<ComponentItem>>() {
    }.getType();

    public ComponentRestService(final RestConnection restConnection) {
        super(restConnection, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public List<ComponentItem> getAllComponents(final String forge, final String groupId, final String artifactId,
            final String version) throws IOException, BDRestException, URISyntaxException {
        final String componentQuery = String.format("id:%s|%s|%s|%s", forge, groupId, artifactId, version);
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(100, COMPONENT_SEGMENTS, componentQuery);

        final List<ComponentItem> allComponents = getAllHubItems(hubPagedRequest);
        return allComponents;
    }

    public ComponentItem getExactComponentMatch(String forge, String groupId, String artifactId, String version)
            throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
        List<ComponentItem> allComponents = getAllComponents(forge, groupId, artifactId, version);
        for (ComponentItem componentItem : allComponents) {
            if (componentItem.getOriginId() != null) {
                String exactMatch = String.format("%s:%s:%s", groupId, artifactId, version);
                if (componentItem.getOriginId().equals(exactMatch)) {
                    return componentItem;
                }
            }
        }

        throw new UnexpectedHubResponseException("Couldn't find an exact component match.");
    }

}
