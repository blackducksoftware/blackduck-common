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
import java.util.Optional;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
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

    public List<NotificationDetailResult> generateContentDetails(final CommonNotificationView view) {
        final NotificationType type = view.getType();
        final String notificationJson = view.json;
        final JsonObject jsonObject = jsonParser.parse(notificationJson).getAsJsonObject();
        if (NotificationType.POLICY_OVERRIDE.equals(type)) {
            final PolicyOverrideNotificationContent content = gson.fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class);
            return generateContentDetails(view, content);
        } else if (NotificationType.RULE_VIOLATION.equals(type)) {
            final RuleViolationNotificationContent content = gson.fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class);
            return generateContentDetails(view, content);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(type)) {
            final RuleViolationClearedNotificationContent content = gson.fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class);
            return generateContentDetails(view, content);
        } else if (NotificationType.VULNERABILITY.equals(type)) {
            final VulnerabilityNotificationContent content = gson.fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class);
            return generateContentDetails(view, content);
        }
        return Collections.emptyList();
    }

    public List<NotificationDetailResult> generateContentDetails(final CommonNotificationView view, final PolicyOverrideNotificationContent content) {
        final List<NotificationDetailResult> resultList = new ArrayList<>();
        content.policyInfos.forEach(policyInfo -> {
            String componentValue;
            if (content.componentVersion != null) {
                componentValue = null;
            } else {
                componentValue = content.component;
            }
            final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.of(content.projectName), Optional.of(content.projectVersionName),
                    Optional.of(content.projectVersion),
                    Optional.of(content.componentName), Optional.ofNullable(componentValue), Optional.of(content.componentVersionName), Optional.of(content.componentVersion), Optional.of(policyInfo.policyName),
                    Optional.of(policyInfo.policy),
                    Optional.empty(), Optional.empty(), Optional.empty());
            resultList.add(new NotificationDetailResult(content, view.getContentType(), view.getCreatedAt(), view.getType(), detail.getNotificationGroup(), detail.getContentDetailKey(), Optional.ofNullable(view.getNotificationState()),
                    detail));
        });
        return resultList;
    }

    public List<NotificationDetailResult> generateContentDetails(final CommonNotificationView view, final RuleViolationNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationDetailResult> resultList = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.of(content.projectName), Optional.of(content.projectVersionName),
                        Optional.of(content.projectVersion),
                        Optional.of(componentVersionStatus.componentName), Optional.ofNullable(componentValue), Optional.of(componentVersionStatus.componentVersionName), Optional.of(componentVersionStatus.componentVersion),
                        Optional.of(policyName),
                        Optional.of(policyUri), Optional.empty(), Optional.of(componentVersionStatus.componentIssueLink), Optional.empty());
                resultList.add(new NotificationDetailResult(content, view.getContentType(), view.getCreatedAt(), view.getType(), detail.getNotificationGroup(), detail.getContentDetailKey(), Optional.ofNullable(view.getNotificationState()),
                        detail));
            });
        });
        return resultList;
    }

    public List<NotificationDetailResult> generateContentDetails(final CommonNotificationView view, final RuleViolationClearedNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationDetailResult> resultList = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.of(content.projectName), Optional.of(content.projectVersionName),
                        Optional.of(content.projectVersion),
                        Optional.of(componentVersionStatus.componentName), Optional.ofNullable(componentValue), Optional.of(componentVersionStatus.componentVersionName), Optional.of(componentVersionStatus.componentVersion),
                        Optional.of(policyName),
                        Optional.of(policyUri), Optional.empty(), Optional.of(componentVersionStatus.componentIssueLink), Optional.empty());
                resultList.add(new NotificationDetailResult(content, view.getContentType(), view.getCreatedAt(), view.getType(), detail.getNotificationGroup(), detail.getContentDetailKey(), Optional.ofNullable(view.getNotificationState()),
                        detail));
            });
        });
        return resultList;
    }

    public List<NotificationDetailResult> generateContentDetails(final CommonNotificationView view, final VulnerabilityNotificationContent content) {
        final List<NotificationDetailResult> resultList = new ArrayList<>();
        content.affectedProjectVersions.forEach(projectVersion -> {
            final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY, Optional.of(projectVersion.projectName), Optional.of(projectVersion.projectVersionName),
                    Optional.of(projectVersion.projectVersion), Optional.of(content.componentName), Optional.empty(), Optional.of(content.versionName), Optional.of(content.componentVersion), Optional.empty(), Optional.empty(),
                    Optional.ofNullable(content.componentVersionOriginName), Optional.ofNullable(projectVersion.componentIssueUrl), Optional.ofNullable(content.componentVersionOriginId));
            resultList.add(new NotificationDetailResult(content, view.getContentType(), view.getCreatedAt(), view.getType(), detail.getNotificationGroup(), detail.getContentDetailKey(), Optional.ofNullable(view.getNotificationState()),
                    detail));
        });
        return resultList;
    }

}
