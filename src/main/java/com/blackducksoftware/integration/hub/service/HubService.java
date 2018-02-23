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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubPath;
import com.blackducksoftware.integration.hub.api.core.HubPathMultipleResponses;
import com.blackducksoftware.integration.hub.api.core.HubPathSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.core.LinkMultipleResponses;
import com.blackducksoftware.integration.hub.api.core.LinkSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.component.ResourceLink;
import com.blackducksoftware.integration.hub.api.generated.component.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.model.PagedRequest;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class HubService {
    public static final String BOMIMPORT_LINK = "/api/bom-import";
    public static final String SCANSUMMARIES_LINK = "/api/scan-summaries";

    private final RestConnection restConnection;
    private final MetaHandler metaHandler;
    private final HubResponseTransformer hubResponseTransformer;
    private final HubResponsesTransformer hubResponsesTransformer;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubBaseUrl = restConnection.baseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
        this.metaHandler = new MetaHandler(restConnection.logger);
        this.hubResponseTransformer = new HubResponseTransformer(restConnection);
        this.hubResponsesTransformer = new HubResponsesTransformer(restConnection, hubResponseTransformer);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public URL getHubBaseUrl() {
        return hubBaseUrl;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Gson getGson() {
        return gson;
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

    public String getUriFromPath(final String path) throws IntegrationException {
        return HubService.pieceTogetherUri(hubBaseUrl, path);
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends HubResponse> List<T> getAllResponsesFromPath(final HubPathMultipleResponses<T> hubPathMultipleResponses) throws IntegrationException {
        return getResponsesFromPath(hubPathMultipleResponses, true);
    }

    public <T extends HubResponse> List<T> getAllResponsesFromPath(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getResponsesFromPath(hubPathMultipleResponses, requestBuilder, true);
    }

    public <T extends HubResponse> List<T> getResponsesFromPath(final HubPathMultipleResponses<T> hubPathMultipleResponses, final boolean getAll) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(hubBaseUrl, hubPathMultipleResponses.hubPath);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), hubPathMultipleResponses.responseClass, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponsesFromPath(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll) throws IntegrationException {
        return getResponsesFromPath(hubPathMultipleResponses, requestBuilder, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponsesFromPath(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll, final Map<String, Class<? extends T>> typeMap)
            throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(hubBaseUrl, hubPathMultipleResponses.hubPath);
        requestBuilder.uri(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), hubPathMultipleResponses.responseClass, getAll, typeMap);
    }

    public <T extends HubResponse> T getResponseFromPath(final HubPathSingleResponse<T> hubPathSingleResponse) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(hubBaseUrl, hubPathSingleResponse.hubPath);
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return hubResponseTransformer.getResponse(request, hubPathSingleResponse.responseClass);
    }

    // ------------------------------------------------
    // getting responses from a HubView
    // ------------------------------------------------
    public <T extends HubResponse> List<T> getAllResponses(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getResponses(hubView, linkMultipleResponses, true);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(hubView, linkMultipleResponses, requestBuilder, true);
    }

    public <T extends HubResponse> List<T> getResponses(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll) throws IntegrationException {
        final String uri = metaHandler.getFirstLinkSafely(hubView, linkMultipleResponses.link);
        if (StringUtils.isBlank(uri)) {
            return Collections.emptyList();
        }
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.responseClass, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponses(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final Request.Builder requestBuilder, final boolean getAll) throws IntegrationException {
        final String uri = metaHandler.getFirstLinkSafely(hubView, linkMultipleResponses.link);
        if (StringUtils.isBlank(uri)) {
            return Collections.emptyList();
        }
        requestBuilder.uri(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.responseClass, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponses(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final Request.Builder requestBuilder, final boolean getAll, final Map<String, Class<? extends T>> typeMap)
            throws IntegrationException {
        final String uri = metaHandler.getFirstLinkSafely(hubView, linkMultipleResponses.link);
        if (StringUtils.isBlank(uri)) {
            return Collections.emptyList();
        }
        requestBuilder.uri(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.responseClass, getAll, typeMap);
    }

    public <T extends HubResponse> T getResponse(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        final String uri = metaHandler.getFirstLinkSafely(hubView, linkSingleResponse.link);
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return hubResponseTransformer.getResponse(request, linkSingleResponse.responseClass);
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends HubResponse> List<T> getAllResponses(final String uri, final Class<T> responseClass) throws IntegrationException {
        return getResponses(uri, responseClass, true);
    }

    public <T extends HubResponse> List<T> getResponses(final String uri, final Class<T> responseClass, final boolean getAll) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        requestBuilder.uri(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), responseClass, getAll, null);
    }

    public <T extends HubResponse> T getResponse(final String uri, final Class<T> responseClass) throws IntegrationException {
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return hubResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response executeGetRequest(final String uri) throws IntegrationException {
        return restConnection.executeRequest(RequestFactory.createCommonGetRequest(uri));
    }

    public Response executeGetRequestFromPath(final String path) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        return restConnection.executeRequest(RequestFactory.createCommonGetRequest(uri));
    }

    public Response executeRequest(final Request request) throws IntegrationException {
        return restConnection.executeRequest(request);
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public String executePostRequestFromPathAndRetrieveURL(final HubPath path, final Request.Builder requestBuilder) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        requestBuilder.uri(uri);
        return executePostRequestAndRetrieveURL(requestBuilder.build());
    }

    public String executePostRequestAndRetrieveURL(final Request request) throws IntegrationException {
        try (Response response = executeRequest(request)) {
            return response.getHeaderValue("location");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // utility
    // ------------------------------------------------
    public static String pieceTogetherUri(final URL baseUrl, final HubPath hubPath) throws IntegrationException {
        return HubService.pieceTogetherUri(baseUrl, hubPath.getPath());
    }

    public static String pieceTogetherUri(final URL baseUrl, final String path) throws IntegrationException {
        String uri;
        try {
            final URIBuilder uriBuilder = new URIBuilder(baseUrl.toURI());
            uriBuilder.setPath(path);
            uri = uriBuilder.build().toString();
        } catch (final URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
        return uri;
    }

}
