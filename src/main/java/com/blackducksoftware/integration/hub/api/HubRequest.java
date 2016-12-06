/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public HubRequest(RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public JsonObject executeGetForResponseJson() throws HubIntegrationException {
        try {
            HttpUrl httpUrl = buildHttpUrl();
            OkHttpClient client = restConnection.createClient(httpUrl);
            Request request = restConnection.createGetRequest(httpUrl);
            Response response = restConnection.handleExecuteClientCall(client, request);
            if (response.isSuccessful()) {
                final String responseString = response.body().string();
                final JsonObject jsonObject = restConnection.getJsonParser().parse(responseString).getAsJsonObject();
                return jsonObject;
            } else {
                throw new HubIntegrationException("There was a problem getting this item : " + url + ". Error : " + response.message());
            }
        } catch (IOException e) {
            throw new HubIntegrationException("There was a problem getting this item : " + url + ". Error : " + e.getMessage(), e);
        }
    }

    public String executeGetForResponseString() throws HubIntegrationException {
        try {
            HttpUrl httpUrl = buildHttpUrl();
            OkHttpClient client = restConnection.createClient(httpUrl);
            Request request = restConnection.createGetRequest(httpUrl);
            Response response = restConnection.handleExecuteClientCall(client, request);
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new HubIntegrationException("There was a problem getting this item : " + url + ". Error : " + response.message());
            }
        } catch (IOException e) {
            throw new HubIntegrationException("There was a problem getting this item : " + url + ". Error : " + e.getMessage(), e);
        }
    }

    public String executePost(String content) throws HubIntegrationException {
        try {
            HttpUrl httpUrl = buildHttpUrl();
            OkHttpClient client = restConnection.createClient(httpUrl);
            Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(content));
            Response response = restConnection.handleExecuteClientCall(client, request);
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new HubIntegrationException("There was a problem posting this item : " + url + ". Error : " + response.message());
            }
        } catch (IOException e) {
            throw new HubIntegrationException("There was a problem posting this item : " + url + ". Error : " + e.getMessage(), e);
        }
    }

    public String executePost(String mediaType, String content) throws HubIntegrationException {
        try {
            HttpUrl httpUrl = buildHttpUrl();
            OkHttpClient client = restConnection.createClient(httpUrl);
            Request request = restConnection.createPostRequest(httpUrl, restConnection.createJsonRequestBody(mediaType, content));
            Response response = restConnection.handleExecuteClientCall(client, request);
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new HubIntegrationException("There was a problem posting this item : " + url + ". Error : " + response.message());
            }
        } catch (IOException e) {
            throw new HubIntegrationException("There was a problem posting this item : " + url + ". Error : " + e.getMessage(), e);
        }
    }

    public void executeDelete() throws HubIntegrationException {
        try {
            HttpUrl httpUrl = buildHttpUrl();
            OkHttpClient client = restConnection.createClient(httpUrl);
            Request request = restConnection.createDeleteRequest(httpUrl);
            Response response = restConnection.handleExecuteClientCall(client, request);
            if (!response.isSuccessful()) {
                throw new HubIntegrationException("There was a problem deleting this item : " + url + ". Error : " + response.message());
            }
        } catch (IOException e) {
            throw new HubIntegrationException("There was a problem deleting this item : " + url + ". Error : " + e.getMessage(), e);
        }
    }

    public void populateQueryParameters() {
        if (StringUtils.isNotBlank(q)) {
            queryParameters.put(QUERY_Q, q);
        }
    }

    private HttpUrl buildHttpUrl() throws HubIntegrationException {
        populateQueryParameters();
        if (url == null) {
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
