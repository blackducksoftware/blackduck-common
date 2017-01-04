/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.service;

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubResponse;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HubRequestService {
    private final RestConnection restConnection;

    private final HubRequestFactory hubRequestFactory;

    public HubRequestService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory(restConnection);
    }

    public String getString(List<String> urlSegments) throws HubIntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final String s = hubRequest.executeGetForResponseString();
        return s;
    }

    public JsonObject getJsonObject(List<String> urlSegments) throws HubIntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(urlSegments);
        final JsonObject jsonObject = hubRequest.executeGetForResponseJson();
        return jsonObject;
    }

    public <T extends HubResponse> T getItem(final HubRequest hubRequest, Class<T> clazz) throws HubIntegrationException {
        final String response = hubRequest.executeGetForResponseString();
        final T item = getRestConnection().getGson().fromJson(response, clazz);
        item.setJson(response);
        return item;
    }

    public <T extends HubResponse> T getItem(String url, Class<T> clazz) throws HubIntegrationException {
        final HubRequest hubRequest = getHubRequestFactory().createGetRequest(url);
        return getItem(hubRequest, clazz);
    }

    public <T extends HubResponse> T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        final T item = getRestConnection().getGson().fromJson(jsonObject, clazz);
        item.setJson(jsonObject.toString());
        return item;
    }

    public <T extends HubResponse> T getItem(final JsonElement jsonElement, final Class<T> clazz) {
        final T item = getRestConnection().getGson().fromJson(jsonElement, clazz);
        item.setJson(jsonElement.toString());
        return item;
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

}
