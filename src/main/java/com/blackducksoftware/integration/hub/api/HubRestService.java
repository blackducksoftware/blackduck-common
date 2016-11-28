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
package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubListResponse;
import com.blackducksoftware.integration.hub.api.item.ParameterizedListType;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubRestService<T> {
    private final RestConnection restConnection;

    private final HubRequestFactory hubRequestFactory;

    private final Class<T> clazz;

    private final Type listType;

    public HubRestService(final RestConnection restConnection, Class<T> clazz) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.clazz = clazz;
        this.listType = new ParameterizedListType(clazz);
    }

    public HubListResponse<T> getListResponse(HubRequest hubRequest) throws IOException, URISyntaxException, BDRestException {
        final JsonObject jsonObject = hubRequest.executeForResponseJson();
        final int totalCount = jsonObject.get("totalCount").getAsInt();
        final List<T> items = getItems(jsonObject);
        return new HubListResponse<>(totalCount, items);
    }

    public List<T> getItems(HubRequest hubRequest) throws IOException, URISyntaxException, BDRestException {
        final JsonObject jsonObject = hubRequest.executeForResponseJson();
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

    public List<T> getAllItems(final HubPagedRequest hubRequest)
            throws BDRestException, IOException, URISyntaxException {
        final List<T> allItems = new ArrayList<>();

        final HubListResponse<T> firstPage = getListResponse(hubRequest);
        final int totalCount = firstPage.getTotalCount();
        final List<T> items = firstPage.getItems();
        allItems.addAll(items);

        while (allItems.size() < totalCount) {
            final int currentOffset = hubRequest.getOffset();
            final int increasedOffset = currentOffset + items.size();

            hubRequest.setOffset(increasedOffset);
            final HubListResponse<T> nextPage = getListResponse(hubRequest);
            allItems.addAll(nextPage.getItems());
        }

        return allItems;
    }

    public T getItem(final HubRequest hubRequest) throws IOException, BDRestException, URISyntaxException {
        final String response = hubRequest.executeForResponseString();
        return getRestConnection().getGson().fromJson(response, clazz);
    }

    public T getItem(String url) throws IOException, BDRestException, URISyntaxException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(url);
        return getItem(hubRequest);
    }

    public T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        return getRestConnection().getGson().fromJson(jsonObject, clazz);
    }

    public String getString(List<String> urlSegments) throws IOException, URISyntaxException, BDRestException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final String s = hubRequest.executeForResponseString();
        return s;
    }

    public JsonObject getJsonObject(List<String> urlSegments) throws IOException, URISyntaxException, BDRestException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final JsonObject jsonObject = hubRequest.executeForResponseJson();
        return jsonObject;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

}
