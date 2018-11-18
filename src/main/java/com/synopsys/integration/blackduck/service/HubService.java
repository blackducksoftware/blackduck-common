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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
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
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import");
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries");

    private final BlackDuckRestConnection restConnection;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;

    public HubService(final IntLogger logger, final BlackDuckRestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        this.restConnection = restConnection;
        hubBaseUrl = restConnection.getBaseUrl();
        this.jsonParser = jsonParser;
        this.gson = gson;
        final BlackDuckJsonTransformer blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, logger);
        blackDuckResponseTransformer = new BlackDuckResponseTransformer(restConnection, blackDuckJsonTransformer);
        blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(restConnection, blackDuckJsonTransformer);
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

    public String getUri(final BlackDuckPath path) throws IntegrationException {
        return pieceTogetherUri(hubBaseUrl, path.getPath());
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getResponses(blackDuckPathMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(blackDuckPathMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllPageResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllPageResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final boolean getAll) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, getAll).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final boolean getAll) throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, blackDuckPathMultipleResponses.getBlackDuckPath().getPath());
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), blackDuckPathMultipleResponses.getResponseClass(), getAll);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll)
            throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, requestBuilder, getAll).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponses(final BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, final Request.Builder requestBuilder, final boolean getAll)
            throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, blackDuckPathMultipleResponses.getBlackDuckPath().getPath());
        requestBuilder.uri(uri);
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), blackDuckPathMultipleResponses.getResponseClass(), getAll);
    }

    public <T extends BlackDuckResponse> T getResponse(final BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        final String uri = pieceTogetherUri(hubBaseUrl, blackDuckPathSingleResponse.getBlackDuckPath().getPath());
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return blackDuckResponseTransformer.getResponse(request, blackDuckPathSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckView
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(final BlackDuckView blackDuckView, final LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getResponses(blackDuckView, linkMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(final BlackDuckView blackDuckView, final LinkMultipleResponses<T> linkMultipleResponses, final Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(blackDuckView, linkMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final BlackDuckView blackDuckView, final LinkMultipleResponses<T> linkMultipleResponses, final boolean getAll) throws IntegrationException {
        final Optional<String> uri = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Collections.emptyList();
        }
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri.get());
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.getResponseClass(), getAll).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final BlackDuckView blackDuckView, final LinkMultipleResponses<T> linkMultipleResponses, final Request.Builder requestBuilder, final boolean getAll) throws IntegrationException {
        final Optional<String> uri = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Collections.emptyList();
        }
        requestBuilder.uri(uri.get());
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.getResponseClass(), getAll).getItems();
    }

    public <T extends BlackDuckResponse> Optional<T> getResponse(final BlackDuckView blackDuckView, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        final Optional<String> uri = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Optional.empty();
        }
        final Request request = RequestFactory.createCommonGetRequest(uri.get());
        return Optional.of(blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass()));
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(final String uri, final Class<T> responseClass) throws IntegrationException {
        return getResponses(uri, responseClass, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final String uri, final Class<T> responseClass, final boolean getAll) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return getResponses(requestBuilder, responseClass, getAll);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(final Request.Builder requestBuilder, final Class<T> responseClass, final boolean getAll) throws IntegrationException {
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), responseClass, getAll).getItems();
    }

    public <T extends BlackDuckResponse> T getResponse(final String uri, final Class<T> responseClass) throws IntegrationException {
        final Request request = RequestFactory.createCommonGetRequest(uri);
        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // getting responses from a UriSingleResponse
    // ------------------------------------------------
    public <T extends BlackDuckResponse> T getResponse(final UriSingleResponse<T> uriSingleResponse) throws IntegrationException {
        final Request request = RequestFactory.createCommonGetRequest(uriSingleResponse.uri);
        return blackDuckResponseTransformer.getResponse(request, uriSingleResponse.responseClass);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(final String url) throws IntegrationException {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.DELETE).uri(url);
        try (final Response response = executeRequest(requestBuilder.build())) {
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

    public Response executeGetRequest(final BlackDuckPath path) throws IntegrationException {
        final String uri = pieceTogetherUri(restConnection.getBaseUrl(), path.getPath());
        return restConnection.executeRequest(RequestFactory.createCommonGetRequest(uri));
    }

    public Response executeRequest(final BlackDuckPath path, final Request.Builder requestBuilder) throws IntegrationException {
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
    public String executePostRequestAndRetrieveURL(final BlackDuckPath path, final Request.Builder requestBuilder) throws IntegrationException {
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
