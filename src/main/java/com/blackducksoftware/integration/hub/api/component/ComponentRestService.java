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
import java.util.Iterator;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    public List<ComponentItem> getAllComponents(final String id, final String groupId, final String artifactId,
            final String version) throws IOException, BDRestException, URISyntaxException {
        final HubRequest componentItemRequest = new HubRequest(getRestConnection());
        final ComponentQuery componentQuery = new ComponentQuery(id, groupId, artifactId, version);

        componentItemRequest.setMethod(Method.GET);
        componentItemRequest.setLimit(1);
        componentItemRequest.addUrlSegments(COMPONENT_SEGMENTS);
        componentItemRequest.setQ(componentQuery.getQuery());

        final JsonObject jsonObject = componentItemRequest.executeForResponseJson();
        final List<ComponentItem> allComponents = getAll(jsonObject, componentItemRequest);
        return allComponents;
    }

    public ComponentItem getExactComponentMatch(String id, String groupId, String artifactId, String version)
            throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
        List<ComponentItem> allComponents = getAllComponents(id, groupId, artifactId, version);
        Iterator<ComponentItem> it = allComponents.iterator();
        while (it.hasNext()) {
            ComponentItem item = it.next();
            if (item.getOriginId() != null) {
                String[] segments = item.getOriginId().split(":");
                if (segments.length == 3 && segments[0].equals(groupId) && segments[1].equals(artifactId) && segments[2].equals(version)) {
                    return item;
                }
            }
        }
        throw new UnexpectedHubResponseException();
    }

}
