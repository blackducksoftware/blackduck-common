/**
 * hub-common
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubResponsesTransformer {
    private final RestConnection restConnection;
    private final HubResponseTransformer hubResponseTransformer;
    private final JsonParser jsonParser;

    public HubResponsesTransformer(final RestConnection restConnection, final HubResponseTransformer hubResponseTransformer) {
        this.restConnection = restConnection;
        this.hubResponseTransformer = hubResponseTransformer;
        this.jsonParser = restConnection.jsonParser;
    }

    public <T extends HubResponse> List<T> getResponses(final JsonArray responsesArray, final Class<T> clazz) {
        final LinkedList<T> itemList = new LinkedList<>();
        for (final JsonElement element : responsesArray) {
            final T item = hubResponseTransformer.getResponseAs(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    public <T extends HubResponse> List<T> getResponses(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        final LinkedList<T> responseList = new LinkedList<>();
        final JsonElement responsesElement = jsonObject.get("items");
        final JsonArray responsesArray = responsesElement.getAsJsonArray();
        for (final JsonElement element : responsesArray) {
            final T item = hubResponseTransformer.getResponseAs(element, clazz);
            responseList.add(item);
        }
        return responseList;
    }

    public <T extends HubResponse> List<T> getResponses(final JsonObject jsonObject, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        final LinkedList<T> responseList = new LinkedList<>();
        final JsonElement responsesElement = jsonObject.get("items");
        final JsonArray responsesArray = responsesElement.getAsJsonArray();
        for (final JsonElement element : responsesArray) {
            final String type = element.getAsJsonObject().get("type").getAsString();
            Class<? extends T> actualClass = clazz;
            if (typeMap.containsKey(type)) {
                actualClass = typeMap.get(type);
            }
            final T item = hubResponseTransformer.getResponseAs(element, actualClass);
            responseList.add(item);
        }
        return responseList;
    }

    public <T extends HubResponse> List<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz) throws IntegrationException {
        return getResponses(pagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {

        try (Response response = restConnection.executeRequest(pagedRequest)) {
            final String jsonResponse = response.getContentString();
            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            if (typeMap != null) {
                return getResponses(jsonObject, clazz, typeMap);
            } else {
                return getResponses(jsonObject, clazz);
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        }
    }

}
