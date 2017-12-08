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

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaUtility;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseAllViewsManager {
    private final HubResponseViewsManager hubResponseViewsManager;
    private final HubRequestFactory hubRequestFactory;
    private final MetaUtility metaService;
    private final JsonParser jsonParser;

    public HubResponseAllViewsManager(final HubResponseViewsManager hubResponseViewsManager, final HubRequestFactory hubRequestFactory, final MetaUtility metaService, final JsonParser jsonParser) {
        this.hubResponseViewsManager = hubResponseViewsManager;
        this.hubRequestFactory = hubRequestFactory;
        this.metaService = metaService;
        this.jsonParser = jsonParser;
    }

    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz) throws IntegrationException {
        return getAllViewsFromApi(apiSegment, clazz, 100, null);
    }

    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return getAllViewsFromApi(apiSegment, clazz, 100, mediaType);
    }

    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage) throws IntegrationException {
        return getAllViewsFromApi(apiSegment, clazz, itemsPerPage, null);
    }

    public <T extends HubView> List<T> getAllViewsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(itemsPerPage, Arrays.asList(SEGMENT_API, apiSegment));
        return getAllViews(hubPagedRequest, clazz, mediaType);
    }

    public <T extends HubView> List<T> getAllViewsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllViewsFromLinkSafely(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubView> List<T> getAllViewsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        if (!metaService.hasLink(hubView, metaLinkRef)) {
            return Collections.emptyList();
        }

        return getAllViewsFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubView> List<T> getAllViewsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllViewsFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubView> List<T> getAllViewsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final String link = metaService.getFirstLink(hubView, metaLinkRef);
        return getAllViews(link, clazz, mediaType);
    }

    public <T extends HubView> List<T> getAllViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return getAllViews(hubPagedRequest, clazz, null);
    }

    public <T extends HubView> List<T> getAllViews(final String url, final Class<T> clazz) throws IntegrationException {
        return getAllViews(url, clazz, null);
    }

    public <T extends HubView> List<T> getAllViews(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllViews(hubPagedRequest, clazz, mediaType);
    }

    public <T extends HubView> List<T> getAllViews(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final List<T> allViews = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.offset;
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = hubPagedRequest.executeGet(mediaType);
            } else {
                response = hubPagedRequest.executeGet();
            }
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allViews.addAll(hubResponseViewsManager.getViews(jsonObject, clazz));
            while (allViews.size() < totalCount && currentOffset < totalCount) {
                currentOffset += hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                allViews.addAll(hubResponseViewsManager.getViews(hubPagedRequest, clazz, mediaType));
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            IOUtils.closeQuietly(response);
        }
        return allViews;
    }

}
