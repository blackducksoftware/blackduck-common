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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckJsonTransformer {
    private final Gson gson;
    private final IntLogger logger;

    public BlackDuckJsonTransformer(final Gson gson, final IntLogger logger) {
        this.gson = gson;
        this.logger = logger;
    }

    public <T extends BlackDuckResponse> T getResponse(final Response response, final Class<T> clazz) throws IntegrationException {
        final String json = response.getContentString();
        return getResponseAs(json, clazz);
    }

    public <T extends BlackDuckResponse> T getResponseAs(final String json, final Class<T> clazz) throws HubIntegrationException {
        try {
            final JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
            return getResponseAs(jsonElement, clazz);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json with Gson:%s%s", System.lineSeparator(), json));
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> T getResponseAs(final JsonElement jsonElement, final Class<T> clazz) throws HubIntegrationException {
        final String json = gson.toJson(jsonElement);
        try {
            final T blackDuckResponse = gson.fromJson(jsonElement, clazz);

            blackDuckResponse.setGson(gson);
            blackDuckResponse.setJsonElement(jsonElement);
            blackDuckResponse.setJson(json);

            return blackDuckResponse;
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided jsonElement with Gson:%s%s", System.lineSeparator(), json));
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(final String json, final Class<T> clazz) throws IntegrationException {
        try {
            final JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            final int totalCount = jsonObject.get("totalCount").getAsInt();
            final JsonArray items = jsonObject.get("items").getAsJsonArray();
            final List<T> itemList = new ArrayList<>();
            for (final JsonElement jsonElement : items) {
                itemList.add(getResponseAs(jsonElement, clazz));
            }

            return new BlackDuckPageResponse<>(totalCount, itemList);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json responses with Gson:%s%s", System.lineSeparator(), json));
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

}
