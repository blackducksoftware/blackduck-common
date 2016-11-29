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
package com.blackducksoftware.integration.hub.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.api.HubRequestFactory;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubRequestService {
    private final RestConnection restConnection;

    private final HubRequestFactory hubRequestFactory;

    public HubRequestService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory(restConnection);
    }

    public String getString(List<String> urlSegments) throws IOException, URISyntaxException, BDRestException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final String s = hubRequest.executeForResponseString();
        return s;
    }

    public JsonObject getJsonObject(List<String> urlSegments) throws IOException, URISyntaxException, BDRestException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final JsonObject jsonObject = hubRequest.executeForResponseJson();
        return jsonObject;
    }

    public <T> T getItem(final HubRequest hubRequest, Class<T> clazz) throws IOException, BDRestException, URISyntaxException {
        final String response = hubRequest.executeForResponseString();
        return getRestConnection().getGson().fromJson(response, clazz);
    }

    public <T> T getItem(String url, Class<T> clazz) throws IOException, BDRestException, URISyntaxException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(url);
        return getItem(hubRequest, clazz);
    }

    public <T> T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        return getRestConnection().getGson().fromJson(jsonObject, clazz);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

}
