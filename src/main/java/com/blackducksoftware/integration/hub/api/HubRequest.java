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
import java.net.URISyntaxException;
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

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
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

    public JsonObject executeForResponseJson() throws IOException, URISyntaxException, BDRestException {
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
            final int responseCode = clientResource.getResponse().getStatus().getCode();
            if (!restConnection.isSuccess(responseCode)) {
                throw new BDRestException("There was a problem deleting this item : " + url + ". Error Code: " + responseCode,
                        clientResource);
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

    public void addQueryParameter(final String queryParameterName, final String queryParameterValue) {
        queryParameters.put(queryParameterName, queryParameterValue);
    }

    public void addQueryParameters(final Map<String, String> queryParameters) {
        queryParameters.putAll(queryParameters);
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
