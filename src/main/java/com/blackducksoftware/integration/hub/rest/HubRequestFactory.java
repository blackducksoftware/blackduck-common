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
package com.blackducksoftware.integration.hub.rest;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Request;

public class HubRequestFactory {
    private final URL baseUrl;

    public HubRequestFactory(final URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String pieceTogetherURI(final URL baseUrl, final String path) throws IntegrationException {
        try {
            final URIBuilder uriBuilder = new URIBuilder(baseUrl.toURI());
            uriBuilder.setPath(path);
            return uriBuilder.build().toString();
        } catch (final URISyntaxException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public Request createGetRequestFromWrapper(final String uri, final RequestWrapper requestWrapper) throws IntegrationException {
        if (requestWrapper == null) {
            return new Request(uri);
        }
        return new Request(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
    }

    public PagedRequest createGetPagedRequestFromWrapper(final String uri, final RequestWrapper requestWrapper) throws IntegrationException {
        if (requestWrapper == null) {
            return new PagedRequest(uri);
        }
        return new PagedRequest(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(),
                requestWrapper.getAdditionalHeaders(), requestWrapper.getLimitPerRequest(), 0);
    }

    public Request createGetRequestFromPathFromWrapper(final String path, final RequestWrapper requestWrapper) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
        return request;
    }

    public PagedRequest createGetPagedRequestFromPathFromWrapper(final String path, final RequestWrapper requestWrapper) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final PagedRequest pagedRequest = new PagedRequest(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(),
                requestWrapper.getAdditionalHeaders(), requestWrapper.getLimitPerRequest(), 0);
        return pagedRequest;
    }

    public Request createGetRequestFromPath(final String path) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, null, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public Request createGetRequestFromPath(final String path, final Map<String, String> queryParameters) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public Request createGetRequest(final String uri, final String mimeType) {
        final Request request = new Request(uri, null, null, HttpMethod.GET, mimeType, null, null);
        return request;
    }

    public Request createGetRequest(final String uri, final Map<String, String> queryParameters) {
        final Request request = new Request(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequest(final String uri, final Map<String, String> queryParameters) {
        final PagedRequest request = new PagedRequest(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequestFromPath(final String path, final Map<String, String> queryParameters) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final PagedRequest request = new PagedRequest(uri, queryParameters, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequest(final String uri, final String mimeType) {
        final PagedRequest request = new PagedRequest(uri, null, null, HttpMethod.GET, mimeType, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequestWithQ(final String uri, final String q) {
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequestWithQ(final String uri, final String q, final int limit) {
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null, limit, 0);
        return request;
    }

    public PagedRequest createGetPagedRequestFromPathWithQ(final String path, final String q) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null);
        return request;
    }

    public PagedRequest createGetPagedRequestFromPathWithQ(final String path, final String q, final int limit) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final PagedRequest request = new PagedRequest(uri, null, q, HttpMethod.GET, null, null, null, limit, 0);
        return request;
    }

    public PagedRequest createGetPagedRequestFromPath(final String path, final String mimeType, final int limit) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final PagedRequest request = new PagedRequest(uri, null, null, HttpMethod.GET, mimeType, null, null, limit, 0);
        return request;
    }

    public Request createRequest(final String uri, final HttpMethod method) {
        final Request request = new Request(uri, null, null, method, null, null, null);
        return request;
    }

    public Request createRequest(final String uri, final HttpMethod method, final String mimeType) {
        final Request request = new Request(uri, null, null, method, mimeType, null, null);
        return request;
    }

    public Request createRequestFromPath(final String path, final HttpMethod method) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, null, null, method, null, null, null);
        return request;
    }

    public Request createRequestFromPath(final String path, final HttpMethod method, final String mimeType) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, null, null, method, mimeType, null, null);
        return request;
    }

}
