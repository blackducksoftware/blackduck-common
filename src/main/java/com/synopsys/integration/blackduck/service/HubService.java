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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.HubPath;
import com.synopsys.integration.blackduck.api.core.HubPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.HubPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.view.MetaHandler;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class HubService {
    public static final HubPath BOMIMPORT_PATH = new HubPath("/api/bom-import");
    public static final HubPath SCANSUMMARIES_PATH = new HubPath("/api/codelocation-summaries");

    private final BlackDuckRestConnection restConnection;
    private final MetaHandler metaHandler;
    private final HubResponseTransformer hubResponseTransformer;
    private final HubResponsesTransformer hubResponsesTransformer;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubService(final IntLogger logger, final BlackDuckRestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        this.restConnection = restConnection;
        hubBaseUrl = restConnection.getBaseUrl();
        this.jsonParser = jsonParser;
        this.gson = gson;
        metaHandler = new MetaHandler(logger);
        hubResponseTransformer = new HubResponseTransformer(restConnection, gson, jsonParser, logger);
        hubResponsesTransformer = new HubResponsesTransformer(restConnection, hubResponseTransformer, jsonParser, logger);
    }

    public BlackDuckRestConnection getRestConnection() {
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

    public String convertToJson(final Object obj) {
        return gson.toJson(obj);
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

    public String getUri(final HubPath path) throws IntegrationException {
        return pieceTogetherUri(hubBaseUrl, path.getPath());
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends HubResponse> List<T> getAllResponses(final HubPathMultipleResponses<T> hubPathMultipleResponses) throws IntegrationException {
        return getResponses(hubPathMultipleResponses, true);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(hubPathMultipleResponses, requestBuilder, true);
    }

    public <T extends HubResponse> List<T> getResponses(final HubPathMultipleResponses<T> hubPathMultipleResponses, final boolean getAll) throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, hubPathMultipleResponses.hubPath.getPath());
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), hubPathMultipleResponses.responseClass, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponses(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll) throws IntegrationException {
        return getResponses(hubPathMultipleResponses, requestBuilder, getAll, null);
    }

    public <T extends HubResponse> List<T> getResponses(final HubPathMultipleResponses<T> hubPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll, final Map<String, Class<? extends T>> typeMap)
            throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, hubPathMultipleResponses.hubPath.getPath());
        requestBuilder.uri(uri);
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), hubPathMultipleResponses.responseClass, getAll, typeMap);
    }

    public <T extends HubResponse> T getResponse(final HubPathSingleResponse<T> hubPathSingleResponse) throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, hubPathSingleResponse.hubPath.getPath());
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
        return getResponses(requestBuilder, responseClass, getAll);
    }

    public <T extends HubResponse> List<T> getResponses(final Request.Builder requestBuilder, final Class<T> responseClass, final boolean getAll) throws IntegrationException {
        return hubResponsesTransformer.getResponses(new PagedRequest(requestBuilder), responseClass, getAll, null);
    }

    public <T extends HubResponse> T getResponse(final String uri, final Class<T> responseClass) throws IntegrationException {
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return hubResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // getting responses from a UriSingleResponse
    // ------------------------------------------------
    public <T extends HubResponse> T getResponse(final UriSingleResponse<T> uriSingleResponse) throws IntegrationException {
        final Request request = RequestFactory.createCommonGetRequest(uriSingleResponse.uri);
        return hubResponseTransformer.getResponse(request, uriSingleResponse.responseClass);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(final String url) throws IntegrationException {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.DELETE).uri(url);
        try (Response response = executeRequest(requestBuilder.build())) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response executeGetRequest(final String uri) throws IntegrationException {
        return restConnection.executeRequest(RequestFactory.createCommonGetRequest(uri));
    }

    public Response executeGetRequest(final HubPath path) throws IntegrationException {
        final String uri = pieceTogetherUri(restConnection.getBaseUrl(), path.getPath());
        return restConnection.executeRequest(RequestFactory.createCommonGetRequest(uri));
    }

    public Response executeRequest(final HubPath path, final Request.Builder requestBuilder) throws IntegrationException {
        final String uri = pieceTogetherUri(restConnection.getBaseUrl(), path.getPath());
        requestBuilder.uri(uri);
        return executeRequest(requestBuilder.build());
    }

    public Response executeRequest(final Request request) throws IntegrationException {
        return restConnection.executeRequest(request);
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public String executePostRequestAndRetrieveURL(final HubPath path, final Request.Builder requestBuilder) throws IntegrationException {
        final String uri = pieceTogetherUri(restConnection.getBaseUrl(), path.getPath());
        requestBuilder.uri(uri);
        return executePostRequestAndRetrieveURL(requestBuilder.build());
    }

    public String executePostRequestAndRetrieveURL(final Request request) throws IntegrationException {
        try (final Response response = executeRequest(request)) {
            return response.getHeaderValue("location");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private String pieceTogetherUri(final URL baseURL, final String spec) throws HubIntegrationException {
        final URL url;
        try {
            url = new URL(baseURL, spec);
        } catch (final MalformedURLException e) {
            throw new HubIntegrationException(String.format("Could not construct the URL from %s and %s", baseURL.toString(), spec), e);
        }
        return url.toString();
    }

}
