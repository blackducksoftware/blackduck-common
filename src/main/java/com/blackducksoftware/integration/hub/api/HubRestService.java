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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubRestService {
    private final RestConnection restConnection;

    private final HubRequestFactory hubRequestFactory;

    public HubRestService(final RestConnection restConnection) {
        this.restConnection = restConnection;
        this.hubRequestFactory = new HubRequestFactory(restConnection);
    }

    public <T> T getItem(final JsonObject jsonObject, final Class<T> clazz) {
        return getRestConnection().getGson().fromJson(jsonObject, clazz);
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

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

}
