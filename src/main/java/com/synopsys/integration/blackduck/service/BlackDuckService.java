/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckComponent;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.json.BlackDuckJsonTransformer;
import com.synopsys.integration.blackduck.service.json.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.service.json.BlackDuckResponseTransformer;
import com.synopsys.integration.blackduck.service.json.BlackDuckResponsesTransformer;
import com.synopsys.integration.blackduck.service.model.PagedRequest;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingBiFunction;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.UrlSupport;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlackDuckService {
    public static final BlackDuckPath BOMIMPORT_PATH = new BlackDuckPath("/api/bom-import");
    public static final BlackDuckPath SCANSUMMARIES_PATH = new BlackDuckPath("/api/scan-summaries");
    public static final BlackDuckPath UPLOADS_PATH = new BlackDuckPath("/api/uploads");
    public static final BlackDuckPath SCAN_DATA_PATH = new BlackDuckPath("/api/scan/data/"); // TODO: Determine why this endpoint requires a slash at the end.

    private final BlackDuckHttpClient blackDuckHttpClient;
    private final BlackDuckJsonTransformer blackDuckJsonTransformer;
    private final BlackDuckResponseTransformer blackDuckResponseTransformer;
    private final BlackDuckResponsesTransformer blackDuckResponsesTransformer;
    private final HttpUrl blackDuckBaseUrl;
    private final Gson gson;
    private final MediaTypeDiscovery mediaTypeDiscovery;
    private final UrlSupport urlSupport;

    public BlackDuckService(BlackDuckHttpClient blackDuckHttpClient, Gson gson, BlackDuckJsonTransformer blackDuckJsonTransformer, BlackDuckResponseTransformer blackDuckResponseTransformer,
                            BlackDuckResponsesTransformer blackDuckResponsesTransformer, MediaTypeDiscovery mediaTypeDiscovery, UrlSupport urlSupport) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.blackDuckBaseUrl = blackDuckHttpClient.getBaseUrl();
        this.gson = gson;
        this.blackDuckJsonTransformer = blackDuckJsonTransformer;
        this.blackDuckResponseTransformer = blackDuckResponseTransformer;
        this.blackDuckResponsesTransformer = blackDuckResponsesTransformer;
        this.mediaTypeDiscovery = mediaTypeDiscovery;
        this.urlSupport = urlSupport;
    }

    public <T extends BlackDuckResponse> T transformResponse(Response response, Class<T> clazz) throws IntegrationException {
        return blackDuckJsonTransformer.getResponse(response, clazz);
    }

    public String convertToJson(Object obj) {
        return gson.toJson(obj);
    }

    public HttpUrl getUrl(BlackDuckPath path) throws IntegrationException {
        return pieceTogetherUri(blackDuckBaseUrl, path.getPath());
    }

    // ------------------------------------------------
    // getting responses from a 'path', which we define as something that looks like '/api/codelocations'
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckPathMultipleResponses, RequestFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder) throws IntegrationException {
        return getBlackDuckPathResponses(blackDuckPathMultipleResponses, requestBuilder, blackDuckResponsesTransformer::getAllResponses);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckPathMultipleResponses, RequestFactory.createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder, int totalLimit) throws IntegrationException {
        return getBlackDuckPathResponses(blackDuckPathMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit));
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, Request.Builder requestBuilder) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathSingleResponse.getBlackDuckPath().getPath());
        requestBuilder.url(url);
        requestBuilder.mimeType(mediaTypeDiscovery.determineMediaType(url.string()));
        return blackDuckResponseTransformer.getResponse(requestBuilder.build(), blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> T getResponse(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        return getResponse(blackDuckPathSingleResponse, RequestFactory.createCommonGetRequestBuilder());
    }

    // ------------------------------------------------
    // getting responses from a BlackDuckView
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return getAllResponses(blackDuckView, linkMultipleResponses, RequestFactory.createCommonGetRequestBuilder());
    }

    public <T extends BlackDuckResponse> List<T> getAllResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Request.Builder requestBuilder) throws IntegrationException {
        return getBlackDuckViewResponses(blackDuckView, linkMultipleResponses, requestBuilder, blackDuckResponsesTransformer::getAllResponses);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, int totalLimit) throws IntegrationException {
        return getSomeResponses(blackDuckView, linkMultipleResponses, RequestFactory.createCommonGetRequestBuilder(), totalLimit);
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Request.Builder requestBuilder, int totalLimit) throws IntegrationException {
        return getBlackDuckViewResponses(blackDuckView, linkMultipleResponses, requestBuilder, (pagedRequest, responseClass) -> blackDuckResponsesTransformer.getSomeResponses(pagedRequest, responseClass, totalLimit));
    }

    public <T extends BlackDuckResponse> Optional<T> getResponse(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        Optional<HttpUrl> uri = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        if (!uri.isPresent()) {
            return Optional.empty();
        }
        HttpUrl url = uri.get();
        Request request = RequestFactory.createCommonGetRequest(url);
        return Optional.of(blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass()));
    }

    // ------------------------------------------------
    // getting responses from a uri
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(HttpUrl url, Class<T> responseClass, int totalLimit) throws IntegrationException {
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(url);
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    // ------------------------------------------------
    // getting responses from a Request.Builder
    // ------------------------------------------------
    public <T extends BlackDuckResponse> List<T> getAllResponses(Request.Builder requestBuilder, Class<T> responseClass) throws IntegrationException {
        return blackDuckResponsesTransformer.getAllResponses(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    public <T extends BlackDuckResponse> List<T> getSomeResponses(Request.Builder requestBuilder, Class<T> responseClass, int totalLimit) throws IntegrationException {
        return blackDuckResponsesTransformer.getSomeResponses(new PagedRequest(requestBuilder), responseClass, totalLimit).getItems();
    }

    public <T extends BlackDuckResponse> T getResponse(HttpUrl url, Class<T> responseClass) throws IntegrationException {
        Request request = RequestFactory.createCommonGetRequest(url);
        return blackDuckResponseTransformer.getResponse(request, responseClass);
    }

    // ------------------------------------------------
    // getting responses from a LinkSingleResponse
    // ------------------------------------------------
    public <T extends BlackDuckResponse> T getResponse(LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        HttpUrl url = new HttpUrl(linkSingleResponse.getLink());
        Request request = RequestFactory.createCommonGetRequest(url);
        return blackDuckResponseTransformer.getResponse(request, linkSingleResponse.getResponseClass());
    }

    // ------------------------------------------------
    // handling generic post
    // ------------------------------------------------
    public HttpUrl post(BlackDuckPath blackDuckPath, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, blackDuckPath.getPath());
        return post(url, blackDuckComponent);
    }

    public HttpUrl post(HttpUrl url, BlackDuckComponent blackDuckComponent) throws IntegrationException {
        String json = gson.toJson(blackDuckComponent);
        Request request = RequestFactory.createCommonPostRequestBuilder(url, json).build();
        return executePostRequestAndRetrieveURL(request);
    }

    // ------------------------------------------------
    // handling generic delete
    // ------------------------------------------------
    public void delete(BlackDuckView blackDuckView) throws IntegrationException {
        if (blackDuckView.getHref().isPresent()) {
            HttpUrl url = blackDuckView.getHref().get();
            delete(url);
        }
    }

    public void delete(HttpUrl url) throws IntegrationException {
        Request request = new Request.Builder(url, HttpMethod.DELETE).build();
        try (Response response = execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------
    // handling generic put
    // ------------------------------------------------
    public void put(BlackDuckView blackDuckView) throws IntegrationException {
        if (blackDuckView.getHref().isPresent()) {
            HttpUrl url = blackDuckView.getHref().get();
            // add the 'missing' pieces back from view that could have been lost
            String json = blackDuckJsonTransformer.producePatchedJson(blackDuckView);
            Request request = RequestFactory.createCommonPutRequestBuilder(url, json).build();
            try (Response response = execute(request)) {
            } catch (IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        }
    }

    // ------------------------------------------------
    // handling generic get
    // ------------------------------------------------
    public Response get(HttpUrl url) throws IntegrationException {
        Request request = RequestFactory.createCommonGetRequest(url);
        return execute(request);
    }

    public Response get(BlackDuckPath path) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, path.getPath());
        Request request = RequestFactory.createCommonGetRequest(url);
        return execute(request);
    }

    // ------------------------------------------------
    // handling plain requests
    // ------------------------------------------------
    public Response execute(BlackDuckPath path, Request.Builder requestBuilder) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, path.getPath());
        requestBuilder.url(url);
        requestBuilder.mimeType(mediaTypeDiscovery.determineMediaType(url.string()));
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
    public HttpUrl executePostRequestAndRetrieveURL(BlackDuckPath path, Request.Builder requestBuilder) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, path.getPath());
        requestBuilder.url(url);
        requestBuilder.mimeType(mediaTypeDiscovery.determineMediaType(url.string()));
        return executePostRequestAndRetrieveURL(requestBuilder.build());
    }

    public HttpUrl executePostRequestAndRetrieveURL(Request request) throws IntegrationException {
        try (Response response = execute(request)) {
            return new HttpUrl(response.getHeaderValue("location"));
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private <T extends BlackDuckResponse> List<T> getBlackDuckPathResponses(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, Request.Builder requestBuilder,
                                                                            ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        HttpUrl url = pieceTogetherUri(blackDuckBaseUrl, blackDuckPathMultipleResponses.getBlackDuckPath().getPath());
        return getSpecialResponses(() -> Optional.of(url), blackDuckPathMultipleResponses.getResponseClass(), requestBuilder,
                responsesTransformer);
    }

    private <T extends BlackDuckResponse> List<T> getBlackDuckViewResponses(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, Request.Builder requestBuilder,
                                                                            ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        return getSpecialResponses(() -> blackDuckView.getFirstLink(linkMultipleResponses.getLink()), linkMultipleResponses.getResponseClass(), requestBuilder, responsesTransformer);
    }

    private <T extends BlackDuckResponse> List<T> getSpecialResponses(ThrowingSupplier<Optional<HttpUrl>, BlackDuckIntegrationException> urlSupplier, Class<T> responseClass, Request.Builder requestBuilder, ThrowingBiFunction<PagedRequest, Class<T>, BlackDuckPageResponse<T>, IntegrationException> responsesTransformer) throws IntegrationException {
        Optional<HttpUrl> optionalUrl = urlSupplier.get();
        if (!optionalUrl.isPresent()) {
            return Collections.emptyList();
        }
        HttpUrl url = optionalUrl.get();
        requestBuilder.url(url);
        requestBuilder.mimeType(mediaTypeDiscovery.determineMediaType(url.string()));
        return responsesTransformer.apply(new PagedRequest(requestBuilder), responseClass).getItems();
    }

    private HttpUrl pieceTogetherUri(String baseUrl, String spec) throws IntegrationException {
        return urlSupport.appendRelativeUrl(baseUrl, spec);
    }

    private HttpUrl pieceTogetherUri(HttpUrl baseUrl, String spec) throws IntegrationException {
        return urlSupport.appendRelativeUrl(baseUrl.string(), spec);
    }

}
