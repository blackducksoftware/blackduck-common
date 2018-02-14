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
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.core.LinkMultipleResponses;
import com.blackducksoftware.integration.hub.api.core.LinkSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.component.ResourceLink;
import com.blackducksoftware.integration.hub.api.generated.component.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.RequestWrapper;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
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
        this.hubResponseTransformer = new HubResponseTransformer(restConnection, metaHandler);
        this.hubResponsesTransformer = new HubResponsesTransformer(restConnection, hubResponseTransformer, metaHandler);
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

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLink(hubView, linkMultipleResponses, getAll);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponseSafely(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLinkSafely(hubView, linkMultipleResponses, getAll);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLink(hubView, linkMultipleResponses, getAll, requestWrapper);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final HubView hubView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll, final RequestWrapper requestWrapper,
            final Map<String, Class<? extends T>> typeMap) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLink(hubView, linkMultipleResponses, getAll, requestWrapper, typeMap);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLinkResponse(linkMultipleResponses, getAll);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLinkResponse(linkMultipleResponses, getAll, requestWrapper);
    }

    public <T extends HubResponse> List<T> getResponsesFromLinkResponse(final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll, final RequestWrapper requestWrapper, final Map<String, Class<? extends T>> typeMap)
            throws IntegrationException {
        return hubResponsesTransformer.getResponsesFromLinkResponse(linkMultipleResponses, getAll, requestWrapper, typeMap);
    }

    public <T extends HubResponse> List<T> getResponses(final String url, final Class<T> clazz, final boolean getAll, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponsesTransformer.getResponses(url, clazz, getAll, requestWrapper);
    }

    public <T extends HubResponse> List<T> getResponses(final String url, final Class<T> clazz, final boolean getAll) throws IntegrationException {
        return hubResponsesTransformer.getResponses(url, clazz, getAll);
    }

    public <T extends HubResponse> T getResponseFromLinkResponse(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLink(hubView, linkSingleResponse);
    }

    public <T extends HubResponse> T getResponseFromLinkResponseSafely(final HubView hubView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLinkSafely(hubView, linkSingleResponse);
    }

    public <T extends HubResponse> T getResponseFromLinkResponse(final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLinkResponse(linkSingleResponse);
    }

    public <T extends HubResponse> T getResponseFromLinkResponse(final LinkSingleResponse<T> linkSingleResponse, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponseTransformer.getResponseFromLinkResponse(linkSingleResponse, requestWrapper);
    }

    public <T extends HubResponse> T getResponseFromPath(final String path, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponseFromPath(path, clazz);
    }

    public <T extends HubResponse> T getResponseFromPath(final String path, final Class<T> clazz, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponseTransformer.getResponseFromPath(path, clazz, requestWrapper);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz) throws IntegrationException {
        return hubResponseTransformer.getResponse(url, clazz);
    }

    public <T extends HubResponse> T getResponse(final String url, final Class<T> clazz, final RequestWrapper requestWrapper) throws IntegrationException {
        return hubResponseTransformer.getResponse(url, clazz, requestWrapper);
    }

    public Response executeGetRequest(final String uri) throws IntegrationException {
        return restConnection.executeRequest(new RequestWrapper().createGetRequest(uri));
    }

    public Response executeGetRequestFromPath(final String path) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        return restConnection.executeRequest(new RequestWrapper().createGetRequest(uri));
    }

    public Response executeGetRequest(final String uri, final RequestWrapper requestWrapper) throws IntegrationException {
        return restConnection.executeRequest(requestWrapper.createGetRequest(uri));
    }

    public Response executeGetRequestFromPath(final String path, final RequestWrapper requestWrapper) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        return restConnection.executeRequest(requestWrapper.createGetRequest(uri));
    }

    public Response executeUpdateRequest(final String uri, final RequestWrapper requestWrapper) throws IntegrationException {
        return restConnection.executeRequest(requestWrapper.createUpdateRequest(uri));
    }

    public Response executeUpdateRequestFromPath(final String path, final RequestWrapper requestWrapper) throws IntegrationException {
        final String uri = HubService.pieceTogetherUri(restConnection.baseUrl, path);
        return restConnection.executeRequest(requestWrapper.createUpdateRequest(uri));
    }

    public String executePostRequestAndRetrieveURL(final String uri, final RequestWrapper requestWrapper) throws IntegrationException {
        try (Response response = executeUpdateRequest(uri, requestWrapper)) {
            return response.getHeaderValue("location");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public String executePostRequestFromPathAndRetrieveURL(final String path, final RequestWrapper requestWrapper) throws IntegrationException {
        try (Response response = executeUpdateRequestFromPath(path, requestWrapper)) {
            return response.getHeaderValue("location");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
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
