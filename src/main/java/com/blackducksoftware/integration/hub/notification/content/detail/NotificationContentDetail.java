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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.util.Stringable;

public class NotificationContentDetail extends Stringable {
    private final NotificationContent notificationContent;
    private final String notificationGroup;
    private final String contentDetailKey;
    private final String projectName;
    private final String projectVersionName;
    private final Optional<UriSingleResponse<ProjectVersionView>> projectVersion;

    private final Optional<String> componentName;
    private final Optional<UriSingleResponse<ComponentView>> component;

    private final Optional<String> componentVersionName;
    private final Optional<UriSingleResponse<ComponentVersionView>> componentVersion;

    private final Optional<String> policyName;
    private final Optional<UriSingleResponse<PolicyRuleViewV2>> policy;

    private final Optional<String> componentVersionOriginName;
    private final Optional<UriSingleResponse<IssueView>> componentIssue;

    private final Optional<String> componentVersionOriginId;

    public final static String CONTENT_KEY_GROUP_LICENSE = "license";
    public final static String CONTENT_KEY_GROUP_POLICY = "policy";
    public final static String CONTENT_KEY_GROUP_VULNERABILITY = "vulnerability";
    public final static String CONTENT_KEY_SEPARATOR = "|";

    public static NotificationContentDetail createDetail(final NotificationContent notificationContent, final String notificationGroup, final String projectName, final String projectVersionName, final String projectVersionUri,
            final String componentName, final String componentUri, final String componentVersionName, final String componentVersionUri, final String policyName, final String policyUri,
            final String componentVersionOriginName, final String componentIssueUri, final String componentVersionOriginId) {
        return new NotificationContentDetail(notificationContent, notificationGroup, projectName, projectVersionName, projectVersion(projectVersionUri),
                Optional.ofNullable(componentName), component(componentUri), Optional.ofNullable(componentVersionName), componentVersion(componentVersionUri),
                Optional.ofNullable(policyName), policy(policyUri),
                Optional.ofNullable(componentVersionOriginName), componentIssue(componentIssueUri), Optional.ofNullable(componentVersionOriginId));
    }

    private NotificationContentDetail(final NotificationContent notificationContent, final String notificationGroup, final String projectName, final String projectVersionName,
            final Optional<UriSingleResponse<ProjectVersionView>> projectVersion,
            final Optional<String> componentName, final Optional<UriSingleResponse<ComponentView>> component, final Optional<String> componentVersionName, final Optional<UriSingleResponse<ComponentVersionView>> componentVersion,
            final Optional<String> policyName, final Optional<UriSingleResponse<PolicyRuleViewV2>> policy, final Optional<String> componentVersionOriginName, final Optional<UriSingleResponse<IssueView>> componentIssue,
            final Optional<String> componentVersionOriginId) {
        this.notificationContent = notificationContent;
        this.notificationGroup = notificationGroup;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.component = component;
        this.componentVersionName = componentVersionName;
        this.componentVersion = componentVersion;
        this.policyName = policyName;
        this.policy = policy;
        this.componentVersionOriginName = componentVersionOriginName;
        this.componentIssue = componentIssue;
        this.componentVersionOriginId = componentVersionOriginId;
        this.contentDetailKey = createContentDetailKey();
    }

    private String createContentDetailKey() {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(notificationGroup);
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (projectVersion.isPresent()) {
            keyBuilder.append(projectVersion.get().uri.hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (component.isPresent()) {
            keyBuilder.append(component.get().uri.hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (componentVersion.isPresent()) {
            keyBuilder.append(componentVersion.get().uri.hashCode());
        }

        if (policy.isPresent()) {
            keyBuilder.append(CONTENT_KEY_SEPARATOR);
            keyBuilder.append(policy.get().uri.hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);
        final String key = keyBuilder.toString();
        return key;
    }

    public boolean hasComponentVersion() {
        return componentVersion.isPresent();
    }

    public boolean hasOnlyComponent() {
        return component.isPresent();
    }

    public boolean isPolicy() {
        return policy.isPresent();
    }

    public boolean isVulnerability() {
        return !isPolicy();
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
        return presentLinks;
    }

    public NotificationContent getNotificationContent() {
        return notificationContent;
    }

    public String getNotificationGroup() {
        return notificationGroup;
    }

    public String getContentDetailKey() {
        return contentDetailKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public Optional<UriSingleResponse<ProjectVersionView>> getProjectVersion() {
        return projectVersion;
    }

    public Optional<String> getComponentName() {
        return componentName;
    }

    public Optional<UriSingleResponse<ComponentView>> getComponent() {
        return component;
    }

    public Optional<String> getComponentVersionName() {
        return componentVersionName;
    }

    public Optional<UriSingleResponse<ComponentVersionView>> getComponentVersion() {
        return componentVersion;
    }

    public Optional<String> getPolicyName() {
        return policyName;
    }

    public Optional<UriSingleResponse<PolicyRuleViewV2>> getPolicy() {
        return policy;
    }

    public Optional<String> getComponentVersionOriginName() {
        return componentVersionOriginName;
    }

    public Optional<UriSingleResponse<IssueView>> getComponentIssue() {
        return componentIssue;
    }

    public Optional<String> getComponentVersionOriginId() {
        return componentVersionOriginId;
    }

    // private methods to assist in static building NotificationContentDetail instances
    private static Optional<UriSingleResponse<ProjectVersionView>> projectVersion(final String projectVersionUri) {
        return optional(projectVersionUri, ProjectVersionView.class);
    }

    private static Optional<UriSingleResponse<ComponentView>> component(final String componentUri) {
        return optional(componentUri, ComponentView.class);
    }

    private static Optional<UriSingleResponse<ComponentVersionView>> componentVersion(final String componentVersionUri) {
        return optional(componentVersionUri, ComponentVersionView.class);
    }

    private static Optional<UriSingleResponse<PolicyRuleViewV2>> policy(final String policyUri) {
        return optional(policyUri, PolicyRuleViewV2.class);
    }

    private static Optional<UriSingleResponse<IssueView>> componentIssue(final String componentIssueUri) {
        return optional(componentIssueUri, IssueView.class);
    }

    private static <T extends HubResponse> Optional<UriSingleResponse<T>> optional(final String uri, final Class<T> responseClass) {
        if (StringUtils.isBlank(uri)) {
            return Optional.empty();
        }
        return Optional.of(new UriSingleResponse<>(uri, responseClass));
    }
}
