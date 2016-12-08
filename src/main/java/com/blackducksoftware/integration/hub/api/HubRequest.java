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

import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_Q;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

/**
 * Most usages of the Hub endpoints as of 2016-11-23 (Hub 3.3.1) should use the HubPagedRequest, but there are several
 * REST endpoints
 * that do not consume limit or offset, and those should use this implementation.
 */
public class HubRequest {
    private final RestConnection restConnection;

    private Method method;

    private String url;

    private final List<String> urlSegments = new ArrayList<>();

    private final Map<String, String> queryParameters = new HashMap<>();

    private String q;

    public HubRequest(RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public JsonObject executeForResponseJson() throws HubIntegrationException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            restConnection.handleRequest(clientResource);

            final Response response = clientResource.getResponse();
            final int responseCode = response.getStatus().getCode();
            if (restConnection.isSuccess(responseCode)) {
                final String responseString = restConnection.readResponseAsString(response);
                final JsonObject jsonObject = restConnection.getJsonParser().parse(responseString).getAsJsonObject();
                return jsonObject;
            } else {
                throw new HubIntegrationException(String.format("Request was not successful. (responseCode: %s)", responseCode));
            }
        } finally {
            releaseResource(clientResource);
        }
    }

    public String executeForResponseString() throws HubIntegrationException {
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
                throw new HubIntegrationException(message);
            }
        } finally {
            releaseResource(clientResource);
        }
    }

    public String executePost(Representation representation) throws HubIntegrationException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            clientResource.getRequest().setEntity(representation);
            return restConnection.handleHttpPost(clientResource);
        } finally {
            releaseResource(clientResource);
        }
    }

    public void executeDelete() throws HubIntegrationException {
        final ClientResource clientResource = buildClientResource(restConnection);
        try {
            restConnection.handleRequest(clientResource);
            final int responseCode = clientResource.getResponse().getStatus().getCode();
            if (!restConnection.isSuccess(responseCode)) {
                throw new HubIntegrationException("There was a problem deleting this item : " + url + ". Error Code: " + responseCode);
            }
        } finally {
            releaseResource(clientResource);
        }
    }

    public void populateQueryParameters() {
        if (StringUtils.isNotBlank(q)) {
            queryParameters.put(QUERY_Q, q);
        }
    }

    private void releaseResource(final ClientResource resource) {
        if (resource.getResponse() != null) {
            resource.getResponse().release();
        }
        resource.release();
    }

    private ClientResource buildClientResource(final RestConnection restConnection) throws HubIntegrationException {
        final ClientResource resource;
        if (StringUtils.isNotBlank(url)) {
            resource = restConnection.createClientResource(url);
        } else {
            resource = restConnection.createClientResource();
        }

        for (final String segment : urlSegments) {
            resource.addSegment(segment);
        }

        populateQueryParameters();

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

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public HubRequest addQueryParameter(final String queryParameterName, final String queryParameterValue) {
        queryParameters.put(queryParameterName, queryParameterValue);
        return this;
    }

    public HubRequest addQueryParameters(final Map<String, String> queryParameters) {
        queryParameters.putAll(queryParameters);
        return this;
    }

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
