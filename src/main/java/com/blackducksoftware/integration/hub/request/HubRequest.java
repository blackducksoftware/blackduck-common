/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc..
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
package com.blackducksoftware.integration.hub.request;

import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_Q;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Most usages of the Hub endpoints as of 2016-11-23 (Hub 3.3.1) should use the HubPagedRequest, but there are several
 * REST endpoints
 * that do not consume limit or offset, and those should use this implementation.
 */
public class HubRequest {
    private final RestConnection restConnection;

    private String url;

    private final List<String> urlSegments = new ArrayList<>();

    private final Map<String, String> queryParameters = new HashMap<>();

    private String q;

    public HubRequest(final RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public JsonObject executeGetForResponseJson() throws HubIntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        try {
            final Request request = restConnection.createGetRequest(httpUrl);
            final Response response = restConnection.handleExecuteClientCall(request);
            final String responseString = response.body().string();
            final JsonObject jsonObject = restConnection.getJsonParser().parse(responseString).getAsJsonObject();
            return jsonObject;
        } catch (final IOException e) {
            throw new HubIntegrationException("There was a problem getting this item : " + httpUrl.uri().toString() + ". Error : " + e.getMessage(), e);
        }
    }

    public String executeGetForResponseString() throws HubIntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        ResponseBody body = null;
        try {
            final Request request = restConnection.createGetRequest(httpUrl);
            final Response response = restConnection.handleExecuteClientCall(request);
            body = response.body();
            return body.string();
        } catch (final IOException e) {
            throw new HubIntegrationException("There was a problem getting this item : " + httpUrl.uri().toString() + ". Error : " + e.getMessage(), e);
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    public String executePost(final String content) throws HubIntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        try {
            final Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(content));
            final Response response = restConnection.handleExecuteClientCall(request);
            return response.header("location");
        } catch (final IOException e) {
            throw new HubIntegrationException("There was a problem posting this item : " + httpUrl.uri().toString() + ". Error : " + e.getMessage(), e);
        }
    }

    public String executePost(final String mediaType, final String content) throws HubIntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        try {
            final Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(mediaType, content));
            final Response response = restConnection.handleExecuteClientCall(request);
            return response.header("location");
        } catch (final IOException e) {
            throw new HubIntegrationException("There was a problem posting this item : " + httpUrl.uri().toString() + ". Error : " + e.getMessage(), e);
        }
    }

    public void executeDelete() throws HubIntegrationException {
        final HttpUrl httpUrl = buildHttpUrl();
        try {
            final Request request = restConnection.createDeleteRequest(httpUrl);
            restConnection.handleExecuteClientCall(request);
        } catch (final IOException e) {
            throw new HubIntegrationException("There was a problem deleting this item : " + httpUrl.uri().toString() + ". Error : " + e.getMessage(), e);
        }
    }

    public void populateQueryParameters() {
        if (StringUtils.isNotBlank(q)) {
            queryParameters.put(QUERY_Q, q);
        }
    }

    private HttpUrl buildHttpUrl() throws HubIntegrationException {
        populateQueryParameters();
        if (StringUtils.isBlank(url)) {
            url = restConnection.getBaseUrl().toString();
        }
        return restConnection.createHttpUrl(url, urlSegments, queryParameters);
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
