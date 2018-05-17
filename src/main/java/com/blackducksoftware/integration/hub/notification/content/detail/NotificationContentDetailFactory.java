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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NotificationContentDetailFactory {
    private final Gson gson;
    private final JsonParser jsonParser;

    public NotificationContentDetailFactory(final Gson gson, final JsonParser jsonParser) {
        this.gson = gson;
        this.jsonParser = jsonParser;
    }

    // TODO there should be a method that takes a NotificationView or NotificationUserView and delegates to this.
    public List<NotificationContentDetail> generateContentDetails(final NotificationType type, final String notificationJson) {
        final JsonObject jsonObject = jsonParser.parse(notificationJson).getAsJsonObject();
        if (NotificationType.POLICY_OVERRIDE.equals(type)) {
            final PolicyOverrideNotificationContent content = gson.fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class);
            return generateContentDetails(content);
        } else if (NotificationType.RULE_VIOLATION.equals(type)) {
            final RuleViolationNotificationContent content = gson.fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class);
            return generateContentDetails(content);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(type)) {
            final RuleViolationClearedNotificationContent content = gson.fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class);
            return generateContentDetails(content);
        } else if (NotificationType.VULNERABILITY.equals(type)) {
            final VulnerabilityNotificationContent content = gson.fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class);
            return generateContentDetails(content);
        }
        return Collections.emptyList();
    }

    public List<NotificationContentDetail> generateContentDetails(final PolicyOverrideNotificationContent content) {
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.policyInfos.forEach(policyInfo -> {
            String componentValue;
            if (content.componentVersion != null) {
                componentValue = null;
            } else {
                componentValue = content.component;
            }
            details.add(NotificationContentDetail.createDetail(content, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, content.projectName, content.projectVersionName, content.projectVersion, content.componentName,
                    componentValue, content.componentVersionName, content.componentVersion, policyInfo.policyName, policyInfo.policy, null, null, null));
        });
        return details;
    }

    public List<NotificationContentDetail> generateContentDetails(final RuleViolationNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                details.add(NotificationContentDetail.createDetail(content, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, content.projectName, content.projectVersionName, content.projectVersion, componentVersionStatus.componentName,
                        componentValue, componentVersionStatus.componentVersionName, componentVersionStatus.componentVersion, policyName, policyUri, null, componentVersionStatus.componentIssueLink, null));
            });
        });
        return details;
    }

    public List<NotificationContentDetail> generateContentDetails(final RuleViolationClearedNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                details.add(NotificationContentDetail.createDetail(content, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, content.projectName, content.projectVersionName, content.projectVersion, componentVersionStatus.componentName,
                        componentValue, componentVersionStatus.componentVersionName, componentVersionStatus.componentVersion, policyName, policyUri, null, componentVersionStatus.componentIssueLink, null));
            });
        });
        return details;
    }

    public List<NotificationContentDetail> generateContentDetails(final VulnerabilityNotificationContent content) {
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.affectedProjectVersions.forEach(projectVersion -> {
            details.add(NotificationContentDetail.createDetail(content, NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY, projectVersion.projectName, projectVersion.projectVersionName, projectVersion.projectVersion,
                    content.componentName, null, content.versionName, content.componentVersion, null, null, content.componentVersionOriginName, projectVersion.componentIssueUrl, content.componentVersionOriginId));
        });
        return details;
    }

}
