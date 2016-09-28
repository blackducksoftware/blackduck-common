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
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class PolicyRestService extends HubItemRestService<PolicyRule> {
	private final List<String> getPolicyRuleSegments = Arrays.asList(UrlConstants.SEGMENT_API,
			UrlConstants.SEGMENT_POLICY_RULES);

	public PolicyRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<PolicyRule>() {
		}.getType(), new TypeToken<List<PolicyRule>>() {
		}.getType());
	}

	public PolicyRule getPolicyRule(final String policyUrl) throws IOException, BDRestException, URISyntaxException {
		return getItem(policyUrl);
	}

	public PolicyRule getPolicyRuleById(final String policyRuleId)
			throws IOException, BDRestException, URISyntaxException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.addAll(getPolicyRuleSegments);
		urlSegments.add(policyRuleId);
		final PolicyRule rule = getItem(urlSegments);
		return rule;
	}

	public List<PolicyRule> getAllPolicyRules() throws IOException, BDRestException, URISyntaxException {
		final HubRequest policyRuleItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		policyRuleItemRequest.setMethod(Method.GET);
		policyRuleItemRequest.setLimit(100);
		policyRuleItemRequest.addUrlSegments(getPolicyRuleSegments);

		final JsonObject jsonObject = policyRuleItemRequest.executeForResponseJson();
		final List<PolicyRule> allPolicyRuleItems = getAll(jsonObject, policyRuleItemRequest);
		return allPolicyRuleItems;
	}

}
