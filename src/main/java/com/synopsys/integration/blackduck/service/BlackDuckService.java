/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class BlackDuckService {
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import");
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries");
    public static final BlackDuckPath UPLOADS_PATH = new BlackDuckPath("/api/uploads");

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final String blackDuckBaseUrl;
    private final Gson gson;
    private final ObjectMapper objectMapper;

    public BlackDuckService(IntLogger logger, BlackDuckHttpClient blackDuckHttpClient, Gson gson, ObjectMapper objectMapper) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        blackDuckBaseUrl = blackDuckHttpClient.getBaseUrl();
        this.gson = gson;
        this.objectMapper = objectMapper;
        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
        blackDuckResponseTransformer = new BlackDuckResponseTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
        blackDuckResponsesTransformer = new BlackDuckResponsesTransformer(blackDuckHttpClient, blackDuckJsonTransformer);
    }

    public BlackDuckHttpClient getBlackDuckHttpClient() {
        return blackDuckHttpClient;
    }

    public String getBlackDuckBaseUrl() {
        return blackDuckBaseUrl;
    }

    public Gson getGson() {
        return gson;
    }

    public <T extends BlackDuckResponse> T transformResponse(Response response, Class<T> clazz) throws IntegrationException {
        return blackDuckJsonTransformer.getResponse(response, clazz);
    }

    public String convertToJson(Object obj) {
        return gson.toJson(obj);
    }

    public String getUri(BlackDuckPath path) throws IntegrationException {
        return pieceTogetherUri(blackDuckBaseUrl, path.getPath());
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getResponses(blackDuckPathMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(blackDuckPathMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllPageResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getAllPageResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, boolean getAll) throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, getAll).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, boolean getAll) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathMultipleResponses.getBlackDuckPath().getPath());
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), blackDuckPathMultipleResponses.getResponseClass(), getAll);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder, boolean getAll)
            throws IntegrationException {
        return getPageResponses(blackDuckPathMultipleResponses, requestBuilder, getAll).getItems();
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getPageResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder, boolean getAll)
            throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathMultipleResponses.getBlackDuckPath().getPath());
        requestBuilder.uri(uri);
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), blackDuckPathMultipleResponses.getResponseClass(), getAll);
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, Request.Builder requestBuilder) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathSingleResponse.getBlackDuckPath().getPath());
        requestBuilder.uri(uri);
        return blackDuckResponseTransformer.getResponse(requestBuilder.build(), blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathSingleResponse.getBlackDuckPath().getPath());
        Request request = RequestFactory.createCommonGetRequest(uri);
        return blackDuckResponseTransformer.getResponse(request, blackDuckPathSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckView
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getResponses(blackDuckView, linkMultipleResponses, true);
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Request.Builder requestBuilder) throws IntegrationException {
        return getResponses(blackDuckView, linkMultipleResponses, requestBuilder, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, boolean getAll) throws IntegrationException {
        Optional<String> uri = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Collections.emptyList();
        }
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri.get());
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.getResponseClass(), getAll).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Request.Builder requestBuilder, boolean getAll) throws IntegrationException {
        Optional<String> uri = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Collections.emptyList();
        }
        requestBuilder.uri(uri.get());
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), linkMultipleResponses.getResponseClass(), getAll).getItems();
    }

    public <T extends BlackDuckResponse> Optional<T> getResponse(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        Optional<String> uri = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        if (!uri.isPresent() || StringUtils.isBlank(uri.get())) {
            return Optional.empty();
        }
        Request request = RequestFactory.createCommonGetRequest(uri.get());
        return Optional.of(blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass()));
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(String uri, Class<T> responseClass) throws IntegrationException {
        return getResponses(uri, responseClass, true);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(String uri, Class<T> responseClass, boolean getAll) throws IntegrationException {
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(uri);
        return getResponses(requestBuilder, responseClass, getAll);
    }

    public <T extends BlackDuckResponse> List<T> getResponses(Request.Builder requestBuilder, Class<T> responseClass, boolean getAll) throws IntegrationException {
        return blackDuckResponsesTransformer.getResponses(new PagedRequest(requestBuilder), responseClass, getAll).getItems();
    }

    public <T extends BlackDuckResponse> T getResponse(String uri, Class<T> responseClass) throws IntegrationException {
        Request request = RequestFactory.createCommonGetRequest(uri);
        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // getting responses from a UriSingleResponse
    // ------------------------------------------------
    public <T extends BlackDuckResponse> T getResponse(UriSingleResponse<T> uriSingleResponse) throws IntegrationException {
        Request request = RequestFactory.createCommonGetRequest(uriSingleResponse.getUri());
        return blackDuckResponseTransformer.getResponse(request, uriSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // handling post with a response
    // ------------------------------------------------
    public <T extends BlackDuckResponse> T post(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, Object object) throws IntegrationException {
        String responseUri = post(blackDuckPathSingleResponse.getBlackDuckPath(), object);
        return getResponse(responseUri, blackDuckPathSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public String post(BlackDuckPath blackDuckPath, Object object) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckHttpClient.getBaseUrl(), blackDuckPath.getPath());
        return post(uri, object);
    }

    public String post(String uri, Object object) throws IntegrationException {
        String json = gson.toJson(object);
        Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(uri).build();
        return executePostRequestAndRetrieveURL(request);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(BlackDuckView blackDuckView) throws IntegrationException {
        if (blackDuckView.getHref().isPresent()) {
            String url = blackDuckView.getHref().get();
            delete(url);
        }
    }

    public void delete(String url) throws IntegrationException {
        Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.DELETE).uri(url);
        try (Response response = execute(requestBuilder.build())) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic put
    // ------------------------------------------------
    public void put(BlackDuckView blackDuckView) throws IntegrationException {
        if (blackDuckView.getHref().isPresent()) {
            String uri = blackDuckView.getHref().get();
            // add the 'missing' pieces back from view that could have been lost
            String json = blackDuckJsonTransformer.producePatchedJson(blackDuckView);
            Request request = RequestFactory.createCommonPutRequestBuilder(json).uri(uri).build();
            try (Response response = execute(request)) {
            } catch (IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(String uri) throws IntegrationException {
        Request request = RequestFactory.createCommonGetRequest(uri);
        return execute(request);
    }

    public Response get(BlackDuckPath path) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckHttpClient.getBaseUrl(), path.getPath());
        Request request = RequestFactory.createCommonGetRequest(uri);
        return execute(request);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response execute(BlackDuckPath path, Request.Builder requestBuilder) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckHttpClient.getBaseUrl(), path.getPath());
        requestBuilder.uri(uri);
        Request request = requestBuilder.build();
        return execute(request);
    }

    public Response execute(Request request) throws IntegrationException {
        Response response = blackDuckHttpClient.execute(request);
        blackDuckHttpClient.throwExceptionForError(response);
        return response;
    }

    // ------------------------------------------------
    // posting and getting location header
    // ------------------------------------------------
    public String executePostRequestAndRetrieveURL(BlackDuckPath path, Request.Builder requestBuilder) throws IntegrationException {
        String uri = pieceTogetherUri(blackDuckHttpClient.getBaseUrl(), path.getPath());
        requestBuilder.uri(uri);
        return executePostRequestAndRetrieveURL(requestBuilder.build());
    }

    public String executePostRequestAndRetrieveURL(Request request) throws IntegrationException {
        try (Response response = execute(request)) {
            return response.getHeaderValue("location");
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private String pieceTogetherUri(String baseUrl, String spec) throws BlackDuckIntegrationException {
        URL url;
        try {
            URL baseURL = new URL(baseUrl);
            url = new URL(baseURL, spec);
        } catch (MalformedURLException e) {
            throw new BlackDuckIntegrationException(String.format("Could not construct the URL from %s and %s", baseUrl, spec), e);
        }
        return url.toString();
    }

}
