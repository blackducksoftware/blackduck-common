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
import com.blackducksoftware.integration.hub.request.RequestWrapper;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class PolicyRuleService extends HubService {

    public PolicyRuleService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String createPolicyRule(final PolicyRuleViewV2 policyRuleViewV2) throws IntegrationException {
        return executePostRequestFromPathAndRetrieveURL(ApiDiscovery.POLICY_RULES_LINK, new RequestWrapper(HttpMethod.POST).setBodyContentObject(policyRuleViewV2));
    }

    public void updatePolicyRule(final PolicyRuleView policyRuleView) throws IntegrationException {
        try (Response response = executeRequestFromPath(getHref(policyRuleView), new RequestWrapper(HttpMethod.PUT).setBodyContentObject(policyRuleView))) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deletePolicyRule(final PolicyRuleView policyRuleView) throws IntegrationException {
        try (Response response = executeRequest(getHref(policyRuleView), new RequestWrapper(HttpMethod.DELETE))) {

        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
