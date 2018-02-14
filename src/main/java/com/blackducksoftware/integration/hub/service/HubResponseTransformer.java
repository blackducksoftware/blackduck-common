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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.core.LinkSingleResponse;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.GetRequestWrapper;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubResponseTransformer {
    private final RestConnection restConnection;
    private final MetaHandler metaHandler;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubResponseTransformer(final RestConnection restConnection, final MetaHandler metaHandler) {
        this.restConnection = restConnection;
        this.metaHandler = metaHandler;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
    }

    public <T extends HubResponse> T getResponseFromLinkSafely(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        if (!metaHandler.hasLink(hubView, linkSingleResponse.link)) {
            return getResponseFromLink(hubView, linkSingleResponse, new GetRequestWrapper());
        } else {
            return null;
        }
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return getResponseFromLink(hubView, linkSingleResponse, new GetRequestWrapper());
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse, final GetRequestWrapper requestWrapper) throws IntegrationException {
        final String link = metaHandler.getFirstLink(hubView, linkSingleResponse.link);
        return getResponse(link, linkSingleResponse.responseClass, requestWrapper);
    }

    public <T extends HubResponse> T getResponse(final String uri, final Class<T> clazz) throws IntegrationException {
        return getResponse(uri, clazz, new GetRequestWrapper());
    }

    public <T extends HubResponse> T getResponse(final String uri, final Class<T> clazz, final GetRequestWrapper requestWrapper) throws IntegrationException {
        return getResponse(requestWrapper.createGetRequest(uri), clazz);
    }

    public <T extends HubResponse> T getResponseFromPath(final String path, final Class<T> clazz) throws IntegrationException {
        return getResponseFromPath(path, clazz, new GetRequestWrapper());
    }

    public <T extends HubResponse> T getResponseFromPath(final String path, final Class<T> clazz, final GetRequestWrapper requestWrapper) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        return getResponse(requestWrapper.createGetRequest(uri), clazz);
    }

    public <T extends HubResponse> T getResponseFromLinkResponse(final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return getResponseFromLinkResponse(linkSingleResponse, new GetRequestWrapper());
    }

    public <T extends HubResponse> T getResponseFromLinkResponse(final LinkSingleResponse<T> linkSingleResponse, final GetRequestWrapper requestWrapper) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, linkSingleResponse.link);
        return getResponse(requestWrapper.createGetRequest(uri), linkSingleResponse.responseClass);
    }

    public <T extends HubResponse> T getResponse(final Request request, final Class<T> clazz) throws IntegrationException {
        try (Response response = restConnection.executeRequest(request)) {
            final String jsonResponse = response.getContentString();
            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
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

    public <T extends HubResponse> T getResponseAs(final String view, final Class<T> clazz) {
        final T hubItem = gson.fromJson(view, clazz);
        hubItem.json = view;
        return hubItem;
    }

}
