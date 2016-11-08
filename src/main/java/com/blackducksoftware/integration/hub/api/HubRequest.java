/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api;

import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_LIMIT;
import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_OFFSET;
import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_Q;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubRequest {
    public static final int EXCLUDE_INTEGER_QUERY_PARAMETER = -999;

    private final RestConnection restConnection;

    private final JsonParser jsonParser;

    private Method method;

    private String url;

    private final List<String> urlSegments = new ArrayList<>();

    private int limit = 10;

    private final Map<String, String> queryParameters = new HashMap<>();

    private int offset = 0;

    private String q;

    public HubRequest(final RestConnection restConnection, final JsonParser jsonParser) {
        this.restConnection = restConnection;
        this.jsonParser = jsonParser;
    }

    public JsonObject executeForResponseJson() throws IOException, URISyntaxException, BDRestException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            restConnection.handleRequest(clientResource);

            final Response response = clientResource.getResponse();
            final int responseCode = response.getStatus().getCode();
            if (restConnection.isSuccess(responseCode)) {
                final String responseString = restConnection.readResponseAsString(response);
                final JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
                return jsonObject;
            } else {
                final String message = String.format("Request was not successful. (responseCode: %s)", responseCode);
                throw new BDRestException(message, clientResource);
            }
        } finally {
            releaseResource(clientResource);
        }
    }

    public String executeForResponseString() throws IOException, URISyntaxException, BDRestException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            restConnection.handleRequest(clientResource);

            final Response response = clientResource.getResponse();
            final int responseCode = response.getStatus().getCode();
            if (restConnection.isSuccess(responseCode)) {
                final String responseString = restConnection.readResponseAsString(response);
                return responseString;
            } else {
                final String message = String.format("Request was not successful. (responseCode: %s): %s", responseCode,
                        clientResource.toString());
                throw new BDRestException(message, clientResource);
            }
        } finally {
            releaseResource(clientResource);
        }
    }

    public String executePost(Representation representation) throws URISyntaxException, IOException, ResourceDoesNotExistException, BDRestException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            clientResource.getRequest().setEntity(representation);
            return restConnection.handleHttpPost(clientResource);
        } finally {
            releaseResource(clientResource);
        }
    }

    public void executeDelete() throws BDRestException, URISyntaxException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            restConnection.handleRequest(clientResource);
        } finally {
            releaseResource(clientResource);
        }
    }

    private void releaseResource(final ClientResource resource) {
        if (resource.getResponse() != null) {
            resource.getResponse().release();
        }
        resource.release();
    }

    private ClientResource buildClientResource(final RestConnection restConnection) throws URISyntaxException {
        final ClientResource resource;
        if (StringUtils.isNotBlank(url)) {
            resource = restConnection.createClientResource(url);
        } else {
            resource = restConnection.createClientResource();
        }

        for (final String segment : urlSegments) {
            resource.addSegment(segment);
        }
        // TODO this will need to be refactored out with the new version of hub
        // common.
        if (limit != EXCLUDE_INTEGER_QUERY_PARAMETER) {
            // if limit is not provided, the default is 10
            if (limit <= 0) {
                limit = 10;
            }
            queryParameters.put(QUERY_LIMIT, String.valueOf(limit));
        }

        if (offset != EXCLUDE_INTEGER_QUERY_PARAMETER) {
            // if offset is not provided, the default is 0
            if (offset != EXCLUDE_INTEGER_QUERY_PARAMETER && offset < 0) {
                offset = 0;
            }
            queryParameters.put(QUERY_OFFSET, String.valueOf(offset));
        }
        if (StringUtils.isNotBlank(q)) {
            queryParameters.put(QUERY_Q, q);
        }

        for (final Map.Entry<String, String> entry : queryParameters.entrySet()) {
            resource.addQueryParameter(entry.getKey(), entry.getValue());
        }

        resource.setMethod(method);
        return resource;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public List<String> getUrlSegments() {
        return urlSegments;
    }

    public void addUrlSegment(final String urlSegment) {
        urlSegments.add(urlSegment);
    }

    public void addUrlSegments(final List<String> urlSegment) {
        urlSegments.addAll(urlSegment);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void addQueryParameter(final String queryParameterName, final String queryParameterValue) {
        queryParameters.put(queryParameterName, queryParameterValue);
    }

    public void addQueryParameters(final Map<String, String> queryParameters) {
        queryParameters.putAll(queryParameters);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q;
    }

    @Override
    public String toString() {
        return "HubRequest [url=" + url + ", urlSegments=" + urlSegments + ", limit=" + limit + ", queryParameters="
                + queryParameters + ", offset=" + offset + ", q=" + q + "]";
    }

}
