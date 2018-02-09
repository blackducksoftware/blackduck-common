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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RequestWrapper;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AllHubResponsesTransformer {
    private final RestConnection restConnection;
    private final HubResponsesTransformer hubResponsesTransformer;
    private final HubRequestFactory hubRequestFactory;
    private final MetaHandler metaHandler;
    private final JsonParser jsonParser;

    public AllHubResponsesTransformer(final RestConnection restConnection, final HubResponsesTransformer hubResponsesTransformer, final HubRequestFactory hubRequestFactory, final MetaHandler metaHandler) {
        this.restConnection = restConnection;
        this.hubResponsesTransformer = hubResponsesTransformer;
        this.hubRequestFactory = hubRequestFactory;
        this.metaHandler = metaHandler;
        this.jsonParser = restConnection.jsonParser;
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz) throws IntegrationException {
        return getAllResponsesFromApi(apiPath, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final RequestWrapper requestWrapper) throws IntegrationException {
        final PagedRequest pagedRequest = hubRequestFactory.createGetPagedRequestFromPathFromWrapper(apiPath, requestWrapper);
        return getAllResponses(pagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromApi(final String apiPath, final Class<T> clazz, final RequestWrapper requestWrapper, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        final PagedRequest pagedRequest = hubRequestFactory.createGetPagedRequestFromPathFromWrapper(apiPath, requestWrapper);
        return getAllResponses(pagedRequest, clazz, typeMap);
    }

    public <T extends HubResponse> List<T> getAllResponses(final String uri, final Class<T> clazz, final RequestWrapper requestWrapper) throws IntegrationException {
        final PagedRequest pagedRequest = hubRequestFactory.createGetPagedRequestFromWrapper(uri, requestWrapper);
        return getAllResponses(pagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        if (!metaHandler.hasLink(hubView, metaLinkRef)) {
            return Collections.emptyList();
        }
        return getAllResponsesFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllResponsesFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final RequestWrapper requestWrapper) throws IntegrationException {
        final String link = metaHandler.getFirstLink(hubView, metaLinkRef);
        final PagedRequest pagedRequest = hubRequestFactory.createGetPagedRequestFromWrapper(link, requestWrapper);
        return getAllResponses(pagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final RequestWrapper requestWrapper, final Map<String, Class<? extends T>> typeMap)
            throws IntegrationException {
        final String link = metaHandler.getFirstLink(hubView, metaLinkRef);
        final PagedRequest pagedRequest = hubRequestFactory.createGetPagedRequestFromWrapper(link, requestWrapper);
        return getAllResponses(pagedRequest, clazz, typeMap);
    }

    public <T extends HubResponse> List<T> getAllResponses(final String uri, final Class<T> clazz) throws IntegrationException {
        final PagedRequest pagedRequest = new PagedRequest(uri);
        return getAllResponses(pagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllResponses(final PagedRequest pagedRequest, final Class<T> clazz, final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        final List<T> allResponses = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = pagedRequest.getOffset();
        try (Response response = restConnection.executeRequest(pagedRequest)) {
            final String jsonResponse = response.getContentString();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allResponses.addAll(hubResponsesTransformer.getResponses(jsonObject, clazz));
            while (allResponses.size() < totalCount && currentOffset < totalCount) {
                currentOffset += pagedRequest.getLimit();
                final PagedRequest offsetPagedRequest = new PagedRequest(pagedRequest.getUri(), pagedRequest.getQueryParameters(), pagedRequest.getQ(), pagedRequest.getMethod(), pagedRequest.getMimeType(), pagedRequest.getBodyEncoding(),
                        pagedRequest.getAdditionalHeaders(), pagedRequest.getLimit(), currentOffset);
                if (typeMap != null) {
                    allResponses.addAll(hubResponsesTransformer.getResponses(offsetPagedRequest, clazz, typeMap));
                } else {
                    allResponses.addAll(hubResponsesTransformer.getResponses(offsetPagedRequest, clazz));
                }
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
        return allResponses;
    }

}
