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
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyStatusRestService extends HubItemRestService<PolicyStatusItem> {
	public PolicyStatusRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<PolicyStatusItem>() {
		}.getType(), new TypeToken<List<PolicyStatusItem>>() {
		}.getType());
	}

	public PolicyStatusItem getPolicyStatusItem(final String projectId, final String versionId)
			throws IOException, URISyntaxException, BDRestException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add(UrlConstants.SEGMENT_API);
		urlSegments.add(UrlConstants.SEGMENT_PROJECTS);
		urlSegments.add(projectId);
		urlSegments.add(UrlConstants.SEGMENT_VERSIONS);
		urlSegments.add(versionId);
		urlSegments.add(UrlConstants.SEGMENT_POLICY_STATUS);

		final HubRequest policyStatusItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		policyStatusItemRequest.setMethod(Method.GET);
		policyStatusItemRequest.setLimit(1);
		policyStatusItemRequest.addUrlSegments(urlSegments);

		final JsonObject jsonObject = policyStatusItemRequest.executeForResponseJson();
		final PolicyStatusItem policyStatusItem = getItem(jsonObject, PolicyStatusItem.class);
		return policyStatusItem;
	}

}
