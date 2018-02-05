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

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.HubResponse;
import com.blackducksoftware.integration.hub.api.HubView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseTransformer {
    private final HubRequestFactory hubRequestFactory;
    private final MetaHandler metaHandler;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubResponseTransformer(final HubRequestFactory hubRequestFactory, final MetaHandler metaHandler, final JsonParser jsonParser, final Gson gson) {
        this.hubRequestFactory = hubRequestFactory;
        this.metaHandler = metaHandler;
        this.jsonParser = jsonParser;
        this.gson = gson;
    }

    public <T extends HubResponse> T getResponseFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getResponseFromLinkSafely(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> T getResponseFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        if (!metaHandler.hasLink(hubView, metaLinkRef)) {
            return getResponseFromLink(hubView, metaLinkRef, clazz, mediaType);
        } else {
            return null;
        }
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getResponseFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final String link = metaHandler.getFirstLink(hubView, metaLinkRef);
        return getResponse(link, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponse(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return getResponse(request, clazz, null);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz) throws IntegrationException {
        return getResponse(url, clazz, null);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubRequest request = hubRequestFactory.createRequest(url);
        return getResponse(request, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponse(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
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
            return getResponseAs(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public <T extends HubResponse> T getResponseAs(final JsonElement view, final Class<T> clazz) {
        final T hubItem = gson.fromJson(view, clazz);
        hubItem.json = gson.toJson(view);
        return hubItem;
    }

    public <T extends HubResponse> T getResponseAs(final String view, final Class<T> clazz) {
        final T hubItem = gson.fromJson(view, clazz);
        hubItem.json = view;
        return hubItem;
    }

}
