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
package com.blackducksoftware.integration.hub.notification.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.util.Stringable;

public class NotificationContentLinks extends Stringable {
    private final Optional<UriSingleResponse<ProjectVersionView>> projectVersion;
    private final Optional<UriSingleResponse<ComponentView>> component;
    private final Optional<UriSingleResponse<ComponentVersionView>> componentVersion;
    private final Optional<UriSingleResponse<PolicyRuleView>> policy;
    private final Optional<UriSingleResponse<IssueView>> componentIssue;

    public static NotificationContentLinks createPolicyLinksWithComponentOnly(final String projectVersionUri, final String componentUri, final String policyUri) {
        return new NotificationContentLinks(projectVersion(projectVersionUri), component(componentUri), Optional.empty(), policy(policyUri), Optional.empty());
    }

    public static NotificationContentLinks createPolicyLinksWithComponentVersion(final String projectVersionUri, final String componentVersionUri, final String policyUri) {
        return new NotificationContentLinks(projectVersion(projectVersionUri), Optional.empty(), componentVersion(componentVersionUri), policy(policyUri), Optional.empty());
    }

    public static NotificationContentLinks createVulnerabilityLinks(final String projectVersionUri, final String componentVersionUri, final String componentIssueUri) {
        return new NotificationContentLinks(projectVersion(projectVersionUri), Optional.empty(), componentVersion(componentVersionUri), Optional.empty(), componentIssue(componentIssueUri));
    }

    private static Optional<UriSingleResponse<ProjectVersionView>> projectVersion(final String projectVersionUri) {
        return optional(projectVersionUri, ProjectVersionView.class);
    }

    private static Optional<UriSingleResponse<ComponentView>> component(final String componentUri) {
        return optional(componentUri, ComponentView.class);
    }

    private static Optional<UriSingleResponse<ComponentVersionView>> componentVersion(final String componentVersionUri) {
        return optional(componentVersionUri, ComponentVersionView.class);
    }

    private static Optional<UriSingleResponse<PolicyRuleView>> policy(final String policyUri) {
        return optional(policyUri, PolicyRuleView.class);
    }

    private static Optional<UriSingleResponse<IssueView>> componentIssue(final String componentIssueUri) {
        return optional(componentIssueUri, IssueView.class);
    }

    private static <T extends HubResponse> Optional<UriSingleResponse<T>> optional(final String uri, final Class<T> responseClass) {
        if (StringUtils.isBlank(uri)) {
            return Optional.empty();
        }
        return Optional.of(new UriSingleResponse<T>(uri, responseClass));
    }

    private NotificationContentLinks(final Optional<UriSingleResponse<ProjectVersionView>> projectVersion, final Optional<UriSingleResponse<ComponentView>> component, final Optional<UriSingleResponse<ComponentVersionView>> componentVersion,
            final Optional<UriSingleResponse<PolicyRuleView>> policy, final Optional<UriSingleResponse<IssueView>> componentIssue) {
        this.projectVersion = projectVersion;
        this.component = component;
        this.componentVersion = componentVersion;
        this.policy = policy;
        this.componentIssue = componentIssue;
    }

    public boolean hasComponentVersion() {
        return componentVersion.isPresent();
    }

    public boolean hasOnlyComponent() {
        return component.isPresent();
    }

    public boolean hasPolicy() {
        return policy.isPresent();
    }

    public boolean hasVulnerability() {
        return componentIssue.isPresent();
    }

    public List<UriSingleResponse<? extends HubResponse>> getPresentLinks() {
        final List<UriSingleResponse<? extends HubResponse>> presentLinks = new ArrayList<>();
        if (projectVersion.isPresent()) {
            presentLinks.add(projectVersion.get());
        }
        if (component.isPresent()) {
            presentLinks.add(component.get());
        }
        if (componentVersion.isPresent()) {
            presentLinks.add(componentVersion.get());
        }
        if (policy.isPresent()) {
            presentLinks.add(policy.get());
        }
        if (componentIssue.isPresent()) {
            presentLinks.add(componentIssue.get());
        }
        return presentLinks;
    }

    public Optional<UriSingleResponse<ProjectVersionView>> getProjectVersion() {
        return projectVersion;
    }

    public Optional<UriSingleResponse<ComponentView>> getComponent() {
        return component;
    }

    public Optional<UriSingleResponse<ComponentVersionView>> getComponentVersion() {
        return componentVersion;
    }

    public Optional<UriSingleResponse<PolicyRuleView>> getPolicy() {
        return policy;
    }

    public Optional<UriSingleResponse<IssueView>> getComponentIssue() {
        return componentIssue;
    }

}
