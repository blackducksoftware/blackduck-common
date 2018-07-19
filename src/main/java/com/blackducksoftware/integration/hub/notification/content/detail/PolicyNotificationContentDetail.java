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
package com.blackducksoftware.integration.hub.notification.content.detail;

import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.enumeration.NotificationTypeGrouping;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;

public class PolicyNotificationContentDetail extends ProjectNotificationContentDetail {
    private final String policyName;
    private final UriSingleResponse<PolicyRuleViewV2> policy;

    private final String firstName;
    private final String lastName;

    // @formatter:off
    public PolicyNotificationContentDetail(
             final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String componentName
            ,final String componentUri
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String policyName
            ,final String policyUri
            ,final String firstName
            ,final String lastName
            ) {
        super(NotificationTypeGrouping.POLICY, projectName, projectVersionName, projectVersionUri, componentName, componentUri, componentVersionName, componentVersionUri);
        this.policyName = policyName;
        this.policy = createUriSingleResponse(policyUri, PolicyRuleViewV2.class);
        this.firstName = firstName;
        this.lastName = lastName;
    }
    // @formatter:on

    public String getPolicyName() {
        return policyName;
    }

    public UriSingleResponse<PolicyRuleViewV2> getPolicy() {
        return policy;
    }

    public Optional<String> getOverriderFirstName() {
        return Optional.ofNullable(firstName);
    }

    public Optional<String> getOverriderLastName() {
        return Optional.ofNullable(lastName);
    }

    @Override
    public List<UriSingleResponse<? extends HubResponse>> getPresentLinks() {
        final List<UriSingleResponse<? extends HubResponse>> presentLinks = super.getPresentLinks();
        if (policy != null) {
            presentLinks.add(policy);
        }
        return presentLinks;
    }

    @Override
    protected StringBuilder createContentDetailKeyBuilder() {
        final StringBuilder keyBuilder = super.createContentDetailKeyBuilder();
        keyBuilder.append(policy.uri.hashCode());

        keyBuilder.append(CONTENT_KEY_SEPARATOR);
        return keyBuilder;
    }

}
