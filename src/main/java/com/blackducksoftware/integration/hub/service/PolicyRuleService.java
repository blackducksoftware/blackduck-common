/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.request.BodyContent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;

public class PolicyRuleService {
    private final HubService hubService;

    public PolicyRuleService(final HubService hubService) {
        this.hubService = hubService;
    }

    public String createPolicyRule(final PolicyRuleViewV2 policyRuleViewV2) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(policyRuleViewV2);
        return hubService.executePostRequestFromPathAndRetrieveURL(ApiDiscovery.POLICY_RULES_LINK, requestBuilder);
    }

    public void updatePolicyRule(final PolicyRuleView policyRuleView) throws IntegrationException {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.PUT).bodyContent(new BodyContent(policyRuleView)).uri(hubService.getHref(policyRuleView));
        try (Response response = hubService.executeRequest(requestBuilder.build())) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deletePolicyRule(final PolicyRuleView policyRuleView) throws IntegrationException {
        final Request.Builder requestBuilder = new Request.Builder().method(HttpMethod.DELETE).uri(hubService.getHref(policyRuleView));
        try (Response response = hubService.executeRequest(requestBuilder.build())) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
