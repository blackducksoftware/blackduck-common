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

import org.apache.http.client.utils.URIBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Request;
import com.google.gson.Gson;

public class HubRequestFactory {
    private final URL baseUrl;
    private final Gson gson;

    public HubRequestFactory(final URL baseUrl, final Gson gson) {
        this.baseUrl = baseUrl;
        this.gson = gson;
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

    public Request createGetRequest(final String uri, final GetRequestWrapper requestWrapper) throws IntegrationException {
        if (requestWrapper == null) {
            return new Request(uri);
        }
        return new Request(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
    }

    public PagedRequest createGetPagedRequest(final String uri, final GetRequestWrapper requestWrapper) throws IntegrationException {
        if (requestWrapper == null) {
            return new PagedRequest(uri);
        }
        return new PagedRequest(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(),
                requestWrapper.getAdditionalHeaders(), requestWrapper.getLimitPerRequest(), 0);
    }

    public Request createGetRequestFromPath(final String path, final GetRequestWrapper requestWrapper) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        if (requestWrapper == null) {
            return new Request(uri);
        }
        final Request request = new Request(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
        return request;
    }

    public PagedRequest createGetPagedRequestFromPath(final String path, final GetRequestWrapper requestWrapper) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        if (requestWrapper == null) {
            return new PagedRequest(uri);
        }
        final PagedRequest pagedRequest = new PagedRequest(uri, requestWrapper.getQueryParameters(), requestWrapper.getQ(), HttpMethod.GET, requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(),
                requestWrapper.getAdditionalHeaders(), requestWrapper.getLimitPerRequest(), 0);
        return pagedRequest;
    }

    public Request createGetRequestFromPath(final String path) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, null, null, HttpMethod.GET, null, null, null);
        return request;
    }

    public Request createRequest(final String uri, final UpdateRequestWrapper requestWrapper) throws IntegrationException {
        final Request request = new Request(uri, null, null, requestWrapper.getMethod(), requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
        if (null != requestWrapper.getHubComponent()) {
            request.setBodyContent(gson.toJson(requestWrapper.getHubComponent()));
        } else if (null != requestWrapper.getJsonObject()) {
            request.setBodyContent(gson.toJson(requestWrapper.getJsonObject()));
        } else if (null != requestWrapper.getBodyContent()) {
            request.setBodyContent(requestWrapper.getBodyContent());
        } else if (null != requestWrapper.getBodyContentMap()) {
            request.setBodyContentMap(requestWrapper.getBodyContentMap());
        } else if (null != requestWrapper.getBodyContentFile()) {
            request.setBodyContentFile(requestWrapper.getBodyContentFile());
        }
        return request;
    }

    public Request createRequestFromPath(final String path, final UpdateRequestWrapper requestWrapper) throws IntegrationException {
        final String uri = pieceTogetherURI(baseUrl, path);
        final Request request = new Request(uri, null, null, requestWrapper.getMethod(), requestWrapper.getMimeType(), requestWrapper.getBodyEncoding(), requestWrapper.getAdditionalHeaders());
        if (null != requestWrapper.getHubComponent()) {
            request.setBodyContent(gson.toJson(requestWrapper.getHubComponent()));
        } else if (null != requestWrapper.getJsonObject()) {
            request.setBodyContent(gson.toJson(requestWrapper.getJsonObject()));
        } else if (null != requestWrapper.getBodyContent()) {
            request.setBodyContent(requestWrapper.getBodyContent());
        } else if (null != requestWrapper.getBodyContentMap()) {
            request.setBodyContentMap(requestWrapper.getBodyContentMap());
        } else if (null != requestWrapper.getBodyContentFile()) {
            request.setBodyContentFile(requestWrapper.getBodyContentFile());
        }
        return request;
    }

}
