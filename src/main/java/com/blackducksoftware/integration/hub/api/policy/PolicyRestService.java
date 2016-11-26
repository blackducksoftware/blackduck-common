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
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_POLICY_RULES;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubPagedRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.reflect.TypeToken;

public class PolicyRestService extends HubItemRestService<PolicyRule> {
    private static final List<String> POLICY_RULE_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_POLICY_RULES);

    private static final Type ITEM_TYPE = new TypeToken<PolicyRule>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<PolicyRule>>() {
    }.getType();

    public PolicyRestService(final RestConnection restConnection) {
        super(restConnection, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public List<PolicyRule> getAllPolicyRules() throws IOException, BDRestException, URISyntaxException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(100, POLICY_RULE_SEGMENTS);

        final List<PolicyRule> allPolicyRuleItems = getAllHubItems(hubPagedRequest);
        return allPolicyRuleItems;
    }

}
