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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class AllHubResponsesTransformer {
    private final HubResponsesTransformer hubResponsesTransformer;
    private final HubRequestFactory hubRequestFactory;
    private final MetaHandler metaHandler;
    private final JsonParser jsonParser;

    public AllHubResponsesTransformer(final HubResponsesTransformer hubResponsesTransformer, final HubRequestFactory hubRequestFactory, final MetaHandler metaHandler, final JsonParser jsonParser) {
        this.hubResponsesTransformer = hubResponsesTransformer;
        this.hubRequestFactory = hubRequestFactory;
        this.metaHandler = metaHandler;
        this.jsonParser = jsonParser;
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz) throws IntegrationException {
        return getAllResponsesFromApi(apiPath, clazz, 100, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return getAllResponsesFromApi(apiPath, clazz, 100, mediaType);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final int itemsPerPage) throws IntegrationException {
        return getAllResponsesFromApi(apiPath, clazz, itemsPerPage, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final int itemsPerPage, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(itemsPerPage, apiPath);
        return getAllResponses(hubPagedRequest, clazz, null, mediaType);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllResponsesFromLinkSafely(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        if (!metaHandler.hasLink(hubView, metaLinkRef)) {
            return Collections.emptyList();
        }

        return getAllResponsesFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllResponsesFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final String link = metaHandler.getFirstLink(hubView, metaLinkRef);
        return getAllResponses(link, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return getAllResponses(hubPagedRequest, clazz, null, null);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return getAllResponses(hubPagedRequest, clazz, typeMap, null);
    }

    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllResponses(hubPagedRequest, clazz, null, null);
    }

    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllResponses(hubPagedRequest, clazz, null, null);
    }

    public <T extends HubResponse> List<T> getAllResponses(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllResponses(hubPagedRequest, clazz, null, mediaType);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap, final String mediaType) throws IntegrationException {
        final List<T> allResponses = new LinkedList<>();
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
            allResponses.addAll(hubResponsesTransformer.getResponses(jsonObject, clazz));
            while (allResponses.size() < totalCount && currentOffset < totalCount) {
                currentOffset += hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                if (typeMap != null) {
                    allResponses.addAll(hubResponsesTransformer.getResponses(hubPagedRequest, clazz, typeMap, mediaType));
                } else {
                    allResponses.addAll(hubResponsesTransformer.getResponses(hubPagedRequest, clazz, mediaType));
                }
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            ResourceUtil.closeQuietly(response);
        }
        return allResponses;
    }

}
