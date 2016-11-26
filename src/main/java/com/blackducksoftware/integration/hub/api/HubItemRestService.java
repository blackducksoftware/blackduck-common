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

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.item.HubItems;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubItemRestService<T extends HubItem> extends HubRestService {
    private final Type itemType;

    private final Type itemListType;

    public HubItemRestService(final RestConnection restConnection, final Type itemType, final Type itemListType) {
        super(restConnection);

        this.itemType = itemType;
        this.itemListType = itemListType;
    }

    public HubItems<T> getHubItems(HubRequest hubRequest) throws IOException, URISyntaxException, BDRestException {
        JsonObject jsonObject = hubRequest.executeForResponseJson();
        final int totalCount = jsonObject.get("totalCount").getAsInt();
        final List<T> items = getItems(jsonObject);
        return new HubItems<>(totalCount, items);
    }

    /**
     * This method can be overridden by subclasses to provide special treatment for extracting the items from the
     * jsonObject.
     */
    public List<T> getItems(JsonObject jsonObject) {
        return getRestConnection().getGson().fromJson(jsonObject.get("items"), itemListType);
    }

    public List<T> getAllHubItems(final HubPagedRequest hubRequest)
            throws BDRestException, IOException, URISyntaxException {
        final List<T> allItems = new ArrayList<>();

        HubItems<T> firstPage = getHubItems(hubRequest);
        final int totalCount = firstPage.getTotalCount();
        List<T> items = firstPage.getItems();
        allItems.addAll(items);

        while (allItems.size() < totalCount) {
            final int currentOffset = hubRequest.getOffset();
            final int increasedOffset = currentOffset + items.size();

            hubRequest.setOffset(increasedOffset);
            HubItems<T> nextPage = getHubItems(hubRequest);
            allItems.addAll(nextPage.getItems());
        }

        return allItems;
    }

    public T getItem(final HubRequest hubRequest) throws IOException, BDRestException, URISyntaxException {
        final String response = hubRequest.executeForResponseString();
        return getRestConnection().getGson().fromJson(response, itemType);
    }

    public T getItem(String url) throws IOException, BDRestException, URISyntaxException {
        HubRequest hubRequest = getHubRequestFactory().createGetRequest(url);
        return getItem(hubRequest);
    }

    public void deleteItem(HubRequest hubRequest) throws IOException, BDRestException, URISyntaxException {
        hubRequest.executeDelete();
    }

    public Type getItemType() {
        return itemType;
    }

    public Type getItemListType() {
        return itemListType;
    }

}
