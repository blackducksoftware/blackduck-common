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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class HubResponseTransformer {
    private final BlackduckRestConnection restConnection;
    private final JsonParser jsonParser;
    private final Gson gson;
    private final IntLogger logger;

    public HubResponseTransformer(final BlackduckRestConnection restConnection, final Gson gson, final JsonParser jsonParser, final IntLogger logger) {
        this.restConnection = restConnection;
        this.jsonParser = jsonParser;
        this.gson = gson;
        this.logger = logger;
    }

    public <T extends HubResponse> T getResponse(final Request request, final Class<T> clazz) throws IntegrationException {
        try (final Response response = restConnection.executeRequest(request)) {
            final String jsonResponse = response.getContentString();
            final JsonObject jsonObject;
            try {
                jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            } catch (final JsonSyntaxException e) {
                logger.error(String.format("Could not parse the provided Json:%s %s", System.lineSeparator(), jsonResponse));
                throw new HubIntegrationException(e.getMessage(), e);
            }
            return getResponseAs(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends HubResponse> T getResponseAs(final JsonElement view, final Class<T> clazz) {
        final T hubItem = gson.fromJson(view, clazz);
        hubItem.json = gson.toJson(view);
        return hubItem;
    }

    public <T extends HubResponse> T getResponseAs(final String view, final Class<T> clazz) throws HubIntegrationException {
        final T hubItem;
        try {
            hubItem = gson.fromJson(view, clazz);
        } catch (final JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided Json:%s %s", System.lineSeparator(), view));
            throw new HubIntegrationException(e.getMessage(), e);
        }
        hubItem.json = view;
        return hubItem;
    }

}
