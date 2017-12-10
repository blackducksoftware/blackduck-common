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
import java.net.URL;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubResponse;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.model.enumeration.AllowEnum;
import com.blackducksoftware.integration.hub.model.view.components.LinkView;
import com.blackducksoftware.integration.hub.model.view.components.MetaView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseService {
    private final MetaService metaService;
    private final HubResponseItemManager hubResponseItemManager;
    private final HubResponseItemsManager hubResponseItemsManager;
    private final HubResponseAllItemsManager hubResponseAllItemsManager;
    private final HubRequestFactory hubRequestFactory;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubResponseService(final RestConnection restConnection) {
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.hubBaseUrl = restConnection.hubBaseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;

        this.metaService = new MetaService(restConnection.logger);
        this.hubResponseItemManager = new HubResponseItemManager(hubRequestFactory, metaService, jsonParser, gson);
        this.hubResponseItemsManager = new HubResponseItemsManager(hubResponseItemManager, jsonParser);
        this.hubResponseAllItemsManager = new HubResponseAllItemsManager(hubResponseItemsManager, hubRequestFactory, metaService, jsonParser);
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

    public boolean hasLink(final HubView item, final String linkKey) throws HubIntegrationException {
        return metaService.hasLink(item, linkKey);
    }

    public String getFirstLink(final HubView item, final String linkKey) throws HubIntegrationException {
        return metaService.getFirstLink(item, linkKey);
    }

    public String getFirstLinkSafely(final HubView item, final String linkKey) {
        return metaService.getFirstLinkSafely(item, linkKey);
    }

    public List<String> getLinks(final HubView item, final String linkKey) throws HubIntegrationException {
        return metaService.getLinks(item, linkKey);
    }

    public MetaView getMetaView(final HubView item) throws HubIntegrationException {
        return metaService.getMetaView(item);
    }

    public List<LinkView> getLinkViews(final HubView item) throws HubIntegrationException {
        return metaService.getLinkViews(item);
    }

    public List<AllowEnum> getAllowedMethods(final HubView item) throws HubIntegrationException {
        return metaService.getAllowedMethods(item);
    }

    public String getHref(final HubView item) throws HubIntegrationException {
        return metaService.getHref(item);
    }

    public <T extends HubResponse> T getItemFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemManager.getItemFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    public <T extends HubResponse> T getItemFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseItemManager.getItemFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> T getItemFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemManager.getItemFromLink(hubView, metaLinkRef, clazz);
    }

    public <T extends HubResponse> T getItemFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseItemManager.getItemFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemManager.getItem(request, clazz);
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemManager.getItem(url, clazz);
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseItemManager.getItem(url, clazz, mediaType);
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseItemManager.getItem(request, clazz, mediaType);
    }

    public <T extends HubResponse> T getItemAs(final JsonElement item, final Class<T> clazz) {
        return hubResponseItemManager.getItemAs(item, clazz);
    }

    public <T extends HubResponse> T getItemAs(final String item, final Class<T> clazz) {
        return hubResponseItemManager.getItemAs(item, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final JsonArray itemsArray, final Class<T> clazz) {
        return hubResponseItemsManager.getItems(itemsArray, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemsManager.getItems(jsonObject, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return hubResponseItemsManager.getItems(hubPagedRequest, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseItemsManager.getItems(hubPagedRequest, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromApi(apiSegment, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromApi(apiSegment, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromApi(apiSegment, clazz, itemsPerPage);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromApi(apiSegment, clazz, itemsPerPage, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromLink(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItemsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItemsFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItems(hubPagedRequest, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItems(url, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItems(url, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubResponseAllItemsManager.getAllItems(hubPagedRequest, clazz, mediaType);
    }

}
