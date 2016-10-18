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
package com.blackducksoftware.integration.hub.api.policy;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_POLICY_STATUS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_PROJECTS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_VERSIONS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

public class PolicyStatusRestService extends HubItemRestService<PolicyStatusItem> {
    private static final Type ITEM_TYPE = new TypeToken<PolicyStatusItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<PolicyStatusItem>>() {
    }.getType();

    public PolicyStatusRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public PolicyStatusItem getPolicyStatusItem(final String projectId, final String versionId)
            throws IOException, URISyntaxException, BDRestException {
        final List<String> urlSegments = new ArrayList<>();
        urlSegments.add(SEGMENT_API);
        urlSegments.add(SEGMENT_PROJECTS);
        urlSegments.add(projectId);
        urlSegments.add(SEGMENT_VERSIONS);
        urlSegments.add(versionId);
        urlSegments.add(SEGMENT_POLICY_STATUS);

        final HubRequest policyStatusItemRequest = new HubRequest(getRestConnection(), getJsonParser());
        policyStatusItemRequest.setMethod(Method.GET);
        policyStatusItemRequest.setLimit(1);
        policyStatusItemRequest.addUrlSegments(urlSegments);

        final JsonObject jsonObject = policyStatusItemRequest.executeForResponseJson();
        final PolicyStatusItem policyStatusItem = getItem(jsonObject, PolicyStatusItem.class);
        return policyStatusItem;
    }

}
