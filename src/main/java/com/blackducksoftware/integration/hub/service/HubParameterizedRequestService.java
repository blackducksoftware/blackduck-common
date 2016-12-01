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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.HubPagedRequest;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.api.item.HubPagedResponse;
import com.blackducksoftware.integration.hub.api.item.ParameterizedListType;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubParameterizedRequestService<T> extends HubRequestService {
    private final Class<T> clazz;

    private final Type listType;

    public HubParameterizedRequestService(final RestConnection restConnection, Class<T> clazz) {
        super(restConnection);
        this.clazz = clazz;
        // we can use this once we don't need to support older versions (2.3.0 for hub-artifactory) anymore
        // listType = TypeToken.getParameterized(List.class, new Type[] { clazz }).getType();
        listType = new ParameterizedListType(clazz);
    }

    public HubPagedResponse<T> getPagedResponse(HubPagedRequest hubPagedRequest) throws IOException, URISyntaxException, BDRestException {
        final JsonObject jsonObject = hubPagedRequest.executeForResponseJson();
        final int totalCount = jsonObject.get("totalCount").getAsInt();
        final List<T> items = getItems(jsonObject);
        return new HubPagedResponse<>(totalCount, items);
    }

    public List<T> getItems(HubPagedRequest hubPagedRequest) throws IOException, URISyntaxException, BDRestException {
        final JsonObject jsonObject = hubPagedRequest.executeForResponseJson();
        final List<T> items = getItems(jsonObject);
        return items;
    }

    /**
     * This method can be overridden by subclasses to provide special treatment for extracting the items from the
     * jsonObject.
     */
    public List<T> getItems(JsonObject jsonObject) {
        return getRestConnection().getGson().fromJson(jsonObject.get("items"), listType);
    }

    public List<T> getAllItems(final HubPagedRequest hubPagedRequest)
            throws BDRestException, IOException, URISyntaxException {
        final List<T> allItems = new ArrayList<>();

        final HubPagedResponse<T> firstPage = getPagedResponse(hubPagedRequest);
        final int totalCount = firstPage.getTotalCount();
        final List<T> items = firstPage.getItems();
        allItems.addAll(items);

        while (allItems.size() < totalCount) {
            final int currentOffset = hubPagedRequest.getOffset();
            final int increasedOffset = currentOffset + items.size();

            hubPagedRequest.setOffset(increasedOffset);
            final HubPagedResponse<T> nextPage = getPagedResponse(hubPagedRequest);
            allItems.addAll(nextPage.getItems());
        }

        return allItems;
    }

    public List<T> getAllItems(List<String> urlSegments) throws BDRestException, IOException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(urlSegments);
        return getAllItems(hubPagedRequest);
    }

    public List<T> getAllItems(String url) throws BDRestException, IOException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(url);
        return getAllItems(hubPagedRequest);
    }

    public T getItem(final HubRequest hubRequest) throws IOException, BDRestException, URISyntaxException {
        return getItem(hubRequest, clazz);
    }

    public T getItem(String url) throws IOException, BDRestException, URISyntaxException {
        return getItem(url, clazz);
    }

}
