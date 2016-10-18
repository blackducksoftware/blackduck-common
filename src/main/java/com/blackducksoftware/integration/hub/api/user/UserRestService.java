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
package com.blackducksoftware.integration.hub.api.user;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_USERS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class UserRestService extends HubItemRestService<UserItem> {
    private static final List<String> USERS_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_USERS);

    private static final Type ITEM_TYPE = new TypeToken<UserItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<UserItem>>() {
    }.getType();

    public UserRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public List<UserItem> getAllUsers() throws URISyntaxException, BDRestException, IOException {
        final HubRequest userRequest = new HubRequest(getRestConnection(), getJsonParser());
        userRequest.setMethod(Method.GET);
        userRequest.addUrlSegments(USERS_SEGMENTS);
        userRequest.setLimit(100);

        final JsonObject jsonObject = userRequest.executeForResponseJson();
        final List<UserItem> allUserItems = getAll(jsonObject, userRequest);
        return allUserItems;
    }

}
