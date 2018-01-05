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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
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

public class HubService {
    private final MetaHandler metaHandler;
    private final HubViewTransformer hubViewManager;
    private final HubViewsTransformer hubViewsTransformer;
    private final AllHubViewsTransformer allHubViewsTransformer;
    private final HubRequestFactory hubRequestFactory;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubService(final RestConnection restConnection) {
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.hubBaseUrl = restConnection.hubBaseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
        this.metaHandler = new MetaHandler(restConnection.logger);
        this.hubViewManager = new HubViewTransformer(hubRequestFactory, metaHandler, jsonParser, gson);
        this.hubViewsTransformer = new HubViewsTransformer(hubViewManager, jsonParser);
        this.allHubViewsTransformer = new AllHubViewsTransformer(hubViewsTransformer, hubRequestFactory, metaHandler, jsonParser);
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

    public MetaView getMetaView(final HubView view) throws HubIntegrationException {
        return metaHandler.getMetaView(view);
    }

    public List<LinkView> getLinkViews(final HubView view) throws HubIntegrationException {
        return metaHandler.getLinkViews(view);
    }

    public List<AllowEnum> getAllowedMethods(final HubView view) throws HubIntegrationException {
        return metaHandler.getAllowedMethods(view);
    }

    public String getHref(final HubView view) throws HubIntegrationException {
        return metaHandler.getHref(view);
    }

    public <T extends HubView> T getViewFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubViewManager.getViewFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    public <T extends HubView> T getViewFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubViewManager.getViewFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubView> T getViewFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return hubViewManager.getViewFromLink(hubView, metaLinkRef, clazz);
    }

    public <T extends HubView> T getViewFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubViewManager.getViewFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubView> T getView(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return hubViewManager.getView(request, clazz);
    }

    public <T extends HubView> T getView(final String url, final Class<T> clazz) throws IntegrationException {
        return hubViewManager.getView(url, clazz);
    }

    public <T extends HubView> T getView(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubViewManager.getView(url, clazz, mediaType);
    }

    public <T extends HubView> T getView(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubViewManager.getView(request, clazz, mediaType);
    }

    public <T extends HubView> T getViewAs(final JsonElement view, final Class<T> clazz) {
        return hubViewManager.getViewAs(view, clazz);
    }

    public <T extends HubView> T getViewAs(final String view, final Class<T> clazz) {
        return hubViewManager.getViewAs(view, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getViews(final JsonArray viewsArray, final Class<T> clazz) {
        return hubViewsTransformer.getViews(viewsArray, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getViews(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        return hubViewsTransformer.getViews(jsonObject, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return hubViewsTransformer.getViews(hubPagedRequest, clazz);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return hubViewsTransformer.getViews(hubPagedRequest, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromApi(apiSegment, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromApi(apiSegment, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final int viewsPerPage) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromApi(apiSegment, clazz, viewsPerPage);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final int viewsPerPage, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromApi(apiSegment, clazz, viewsPerPage, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromLinkSafely(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromLinkSafely(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromLink(hubView, metaLinkRef, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViewsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViewsFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return allHubViewsTransformer.getAllViews(hubPagedRequest, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViews(final String url, final Class<T> clazz) throws IntegrationException {
        return allHubViewsTransformer.getAllViews(url, clazz);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViews(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViews(url, clazz, mediaType);
    }

    /**
     * WILL make further paged requests to get the full list of items
     */
    public <T extends HubView> List<T> getAllViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return allHubViewsTransformer.getAllViews(hubPagedRequest, clazz, mediaType);
    }

}
