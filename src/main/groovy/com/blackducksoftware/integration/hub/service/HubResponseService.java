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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubResponse;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseService {
    private final HubRequestFactory hubRequestFactory;

    private final URL hubBaseUrl;

    private final JsonParser jsonParser;

    private final Gson gson;

    public HubResponseService(final RestConnection restConnection) {
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.hubBaseUrl = restConnection.hubBaseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
    }

    public URL getHubBaseUrl() {
        return hubBaseUrl;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Gson getGson() {
        return gson;
    }

    public <T extends HubResponse> T getItemAs(final JsonElement item, final Class<T> clazz) {
        final T hubItem = gson.fromJson(item, clazz);
        hubItem.json = gson.toJson(item);
        return hubItem;
    }

    public <T extends HubResponse> T getItemAs(final String item, final Class<T> clazz) {
        final T hubItem = gson.fromJson(item, clazz);
        hubItem.json = item;
        return hubItem;
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        Response response = null;
        try {
            response = request.executeGet();
            // the string method closes the body
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItemAs(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        return getItem(request, clazz);
    }

    public <T extends HubResponse> List<T> getItems(final JsonArray itemsArray, final Class<T> clazz) {
        final LinkedList<T> itemList = new LinkedList<>();
        for (final JsonElement element : itemsArray) {
            final T item = getItemAs(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        final LinkedList<T> itemList = new LinkedList<>();
        final JsonElement itemsElement = jsonObject.get("items");
        final JsonArray itemsArray = itemsElement.getAsJsonArray();
        for (final JsonElement element : itemsArray) {
            final T item = getItemAs(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        Response response = null;
        try {
            response = hubPagedRequest.executeGet();
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItems(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        final List<T> allItems = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.offset;
        Response response = null;
        try {
            response = hubPagedRequest.executeGet();
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allItems.addAll(getItems(jsonObject, clazz));
            while (allItems.size() < totalCount && currentOffset < totalCount) {
                currentOffset += hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                allItems.addAll(getItems(hubPagedRequest, clazz));
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return allItems;
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz) throws IntegrationException {
        final HubPagedRequest pagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllItems(pagedRequest, clazz);
    }

}
