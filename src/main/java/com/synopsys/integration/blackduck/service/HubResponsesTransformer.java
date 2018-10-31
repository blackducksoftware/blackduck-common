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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Response;

public class HubResponsesTransformer {
    private final BlackDuckRestConnection restConnection;
    private final HubResponseTransformer hubResponseTransformer;
    private final JsonParser jsonParser;
    private final IntLogger logger;

    public HubResponsesTransformer(final BlackDuckRestConnection restConnection, final HubResponseTransformer hubResponseTransformer, final JsonParser jsonParser, final IntLogger logger) {
        this.restConnection = restConnection;
        this.hubResponseTransformer = hubResponseTransformer;
        this.jsonParser = jsonParser;
        this.logger = logger;
    }

    public <T extends HubResponse> List<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz) throws IntegrationException {
        return getResponses(pagedRequest, clazz, true, null);
    }

    public <T extends HubResponse> List<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz, final boolean getAll) throws IntegrationException {
        return getResponses(pagedRequest, clazz, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponses(final PagedRequest pagedRequest, final Class<T> clazz, final boolean getAll, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        final List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        try (final Response initialResponse = restConnection.executeRequest(pagedRequest.createRequest())) {
            final String initialJsonResponse = initialResponse.getContentString();
            final JsonObject initialJsonObject;
            try {
                initialJsonObject = jsonParser.parse(initialJsonResponse).getAsJsonObject();
            } catch (final JsonSyntaxException e) {
                logger.error(String.format("Could not parse the initial provided Json responses with JsonParser:%s%s", System.lineSeparator(), initialJsonResponse));
                throw new HubIntegrationException(e.getMessage(), e);
            }
            if (typeMap != null) {
                allResponses.addAll(getResponses(initialJsonObject, clazz, typeMap));
            } else {
                allResponses.addAll(getResponses(initialJsonObject, clazz));
            }
            if (getAll) {
                totalCount = initialJsonObject.get("totalCount").getAsInt();
            } else {
                return allResponses;
            }
            while (allResponses.size() < totalCount && currentOffset < totalCount) {
                currentOffset += pagedRequest.getLimit();
                final PagedRequest offsetPagedRequest = new PagedRequest(pagedRequest.getRequestBuilder(), currentOffset, pagedRequest.getLimit());
                try (final Response response = restConnection.executeRequest(offsetPagedRequest.createRequest())) {
                    final String jsonResponse = response.getContentString();
                    final JsonObject jsonObject;
                    try {
                        jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
                    } catch (final JsonSyntaxException e) {
                        logger.error(String.format("Could not parse the provided Json responses with JsonParser:%s%s", System.lineSeparator(), jsonResponse));
                        throw new HubIntegrationException(e.getMessage(), e);
                    }
                    if (typeMap != null) {
                        allResponses.addAll(getResponses(jsonObject, clazz, typeMap));
                    } else {
                        allResponses.addAll(getResponses(jsonObject, clazz));
                    }
                } catch (final IOException e) {
                    throw new HubIntegrationException(e);
                }
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
        return allResponses;

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
            Class<? extends T> actualClass = clazz;
            final JsonObject elementObject = element.getAsJsonObject();
            if (elementObject.has("type")) {
                final String type = elementObject.get("type").getAsString();
                if (typeMap.containsKey(type)) {
                    actualClass = typeMap.get(type);
                }
            }
            final T item = hubResponseTransformer.getResponseAs(element, actualClass);
            responseList.add(item);
        }
        return responseList;
    }

}
