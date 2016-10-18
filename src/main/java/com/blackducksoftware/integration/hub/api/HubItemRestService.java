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

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubItemRestService<T extends HubItem> extends HubRestService {
    private final Gson gson;

    private final JsonParser jsonParser;

    private final Type itemType;

    private final Type itemListType;

    public HubItemRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser,
            final Type itemType, final Type itemListType) {
        super(restConnection);

        this.gson = gson;
        this.jsonParser = jsonParser;
        this.itemType = itemType;
        this.itemListType = itemListType;
    }

    public List<T> getAll(final JsonObject jsonObject, final HubRequest hubRequest)
            throws BDRestException, IOException, URISyntaxException {
        final List<T> allItems = new ArrayList<>();
        final int totalCount = jsonObject.get("totalCount").getAsInt();
        List<T> items = getItems(jsonObject);
        allItems.addAll(items);

        while (allItems.size() < totalCount) {
            final int currentOffset = hubRequest.getOffset();
            final int increasedOffset = currentOffset + items.size();

            hubRequest.setOffset(increasedOffset);
            final JsonObject nextResponse = hubRequest.executeForResponseJson();
            items = getItems(nextResponse);
            allItems.addAll(items);
        }

        return allItems;
    }

    public List<T> getItems(final JsonObject jsonObject) {
        final List<T> items = gson.fromJson(jsonObject.get("items"), itemListType);
        return items;
    }

    public List<T> getItems(final String url) throws IOException, URISyntaxException, BDRestException {
        final HubRequest itemRequest = new HubRequest(getRestConnection(), jsonParser);
        itemRequest.setMethod(Method.GET);
        itemRequest.setUrl(url);

        final String response = itemRequest.executeForResponseString();
        return gson.fromJson(response, itemListType);
    }

    public T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        return gson.fromJson(jsonObject, clazz);
    }

    public T getItem(final String url) throws IOException, BDRestException, URISyntaxException {
        final HubRequest itemRequest = new HubRequest(getRestConnection(), jsonParser);
        itemRequest.setMethod(Method.GET);
        itemRequest.setUrl(url);
        itemRequest.setOffset(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        itemRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);

        final String response = itemRequest.executeForResponseString();
        return gson.fromJson(response, itemType);
    }

    public T getItem(final List<String> urlSegments) throws IOException, BDRestException, URISyntaxException {
        final HubRequest itemRequest = new HubRequest(getRestConnection(), jsonParser);
        itemRequest.setMethod(Method.GET);
        itemRequest.addUrlSegments(urlSegments);
        itemRequest.setOffset(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);
        itemRequest.setLimit(HubRequest.EXCLUDE_INTEGER_QUERY_PARAMETER);

        final String response = itemRequest.executeForResponseString();
        return gson.fromJson(response, itemType);
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Type getItemType() {
        return itemType;
    }

    public Type getItemListType() {
        return itemListType;
    }

}
