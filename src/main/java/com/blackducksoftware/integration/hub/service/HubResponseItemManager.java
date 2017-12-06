/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubResponse;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseItemManager {
    private final HubRequestFactory hubRequestFactory;
    private final MetaService metaService;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubResponseItemManager(final HubRequestFactory hubRequestFactory, final MetaService metaService, final JsonParser jsonParser, final Gson gson) {
        this.hubRequestFactory = hubRequestFactory;
        this.metaService = metaService;
        this.jsonParser = jsonParser;
        this.gson = gson;
    }

    public <T extends HubResponse> T getItemFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getItemFromLinkSafely(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> T getItemFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        if (!metaService.hasLink(hubView, metaLinkRef)) {
            return getItemFromLink(hubView, metaLinkRef, clazz, mediaType);
        } else {
            return null;
        }
    }

    public <T extends HubResponse> T getItemFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getItemFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> T getItemFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final String link = metaService.getFirstLink(hubView, metaLinkRef);
        return getItem(link, clazz, mediaType);
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return getItem(request, clazz, null);
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz) throws IntegrationException {
        return getItem(url, clazz, null);
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubRequest request = hubRequestFactory.createRequest(url);
        return getItem(request, clazz, mediaType);
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = request.executeGet(mediaType);
            } else {
                response = request.executeGet();
            }
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

}
