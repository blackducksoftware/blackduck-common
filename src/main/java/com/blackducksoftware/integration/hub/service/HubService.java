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
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.generated.model.ResourceLink;
import com.blackducksoftware.integration.hub.api.generated.model.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubService {
    private final RestConnection restConnection;
    private final MetaHandler metaHandler;
    private final HubResponseTransformer hubResponseTransformer;
    private final HubResponsesTransformer hubResponsesTransformer;
    private final AllHubResponsesTransformer allHubResponsesTransformer;
    private final HubRequestFactory hubRequestFactory;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory();
        this.hubBaseUrl = restConnection.baseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
        this.metaHandler = new MetaHandler(restConnection.logger);
        this.hubResponseTransformer = new HubResponseTransformer(hubRequestFactory, metaHandler, jsonParser, gson);
        this.hubResponsesTransformer = new HubResponsesTransformer(hubResponseTransformer, jsonParser);
        this.allHubResponsesTransformer = new AllHubResponsesTransformer(hubResponsesTransformer, hubRequestFactory, metaHandler, jsonParser);
    }

    public RestConnection getRestConnection() {
        return restConnection;
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

    public String readResponseString(final Response response) throws IntegrationException {
        try {
            return response.body().string();
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public boolean hasLink(final HubView view, final String linkKey) throws HubIntegrationException {
        return metaHandler.hasLink(view, linkKey);
    }

    public String getFirstLink(final HubView view, final String linkKey) throws HubIntegrationException {
        return metaHandler.getFirstLink(view, linkKey);
    }

    public String getFirstLinkSafely(final HubView view, final String linkKey) {
        return metaHandler.getFirstLinkSafely(view, linkKey);
    }

    public List<String> getLinks(final HubView view, final String linkKey) throws HubIntegrationException {
        return metaHandler.getLinks(view, linkKey);
    }

    public ResourceMetadata getMetaView(final HubView view) throws HubIntegrationException {
        return metaHandler.getMetaView(view);
    }

    public List<ResourceLink> getLinkViews(final HubView view) throws HubIntegrationException {
        return metaHandler.getLinkViews(view);
    }

    public List<String> getAllowedMethods(final HubView view) throws HubIntegrationException {
        return metaHandler.getAllowedMethods(view);
    }

    public String getHref(final HubView view) throws HubIntegrationException {
        return metaHandler.getHref(view);
    }

    public <T extends HubResponse> T getResponseFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    public <T extends HubResponse> T getResponseFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLink(hubView, metaLinkRef, clazz);
    }

    public <T extends HubResponse> T getResponseFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponse(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponse(request, clazz);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponse(url, clazz);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseTransformer.getResponse(url, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponse(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseTransformer.getResponse(request, clazz, mediaType);
    }

    public <T extends HubResponse> T getResponseAs(final JsonElement view, final Class<T> clazz) {
        return hubResponseTransformer.getResponseAs(view, clazz);
    }

    public <T extends HubResponse> T getResponseAs(final String view, final Class<T> clazz) {
        return hubResponseTransformer.getResponseAs(view, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final JsonArray viewsArray, final Class<T> clazz) {
        return hubResponsesTransformer.getResponses(viewsArray, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        return hubResponsesTransformer.getResponses(jsonObject, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final JsonObject jsonObject, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return hubResponsesTransformer.getResponses(jsonObject, clazz, typeMap);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return hubResponsesTransformer.getResponses(hubPagedRequest, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponsesTransformer.getResponses(hubPagedRequest, clazz, mediaType);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return hubResponsesTransformer.getResponses(hubPagedRequest, clazz, typeMap, null);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap, final String mediaType) throws IntegrationException {
        return hubResponsesTransformer.getResponses(hubPagedRequest, clazz, typeMap, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromApi(apiPath, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromApi(apiPath, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final int viewsPerPage) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromApi(apiPath, clazz, viewsPerPage);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final int viewsPerPage, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromApi(apiPath, clazz, viewsPerPage, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromLink(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponsesFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(hubPagedRequest, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(url, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(url, clazz, typeMap);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(url, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(hubPagedRequest, clazz, null, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(hubPagedRequest, clazz, typeMap, null);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap, final String mediaType) throws IntegrationException {
        return allHubResponsesTransformer.getAllResponses(hubPagedRequest, clazz, typeMap, mediaType);
    }

}
