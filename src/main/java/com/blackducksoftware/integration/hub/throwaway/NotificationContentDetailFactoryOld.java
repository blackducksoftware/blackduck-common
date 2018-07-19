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
package com.blackducksoftware.integration.hub.throwaway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NotificationContentDetailFactoryOld {
    private final Gson gson;
    private final JsonParser jsonParser;

    public NotificationContentDetailFactoryOld(final Gson gson, final JsonParser jsonParser) {
        this.gson = gson;
        this.jsonParser = jsonParser;
    }

    public NotificationDetailResultOld generateContentDetails(final CommonNotificationView view) {
        final NotificationType type = view.getType();
        final String notificationJson = view.json;
        final JsonObject jsonObject = jsonParser.parse(notificationJson).getAsJsonObject();

        NotificationContent notificationContent = null;
        String notificationGroup = null;
        final List<NotificationContentDetailOld> notificationContentDetails = new ArrayList<>();

        if (NotificationType.POLICY_OVERRIDE.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class);
            notificationGroup = NotificationContentDetailOld.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (PolicyOverrideNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class);
            notificationGroup = NotificationContentDetailOld.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (RuleViolationNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class);
            notificationGroup = NotificationContentDetailOld.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (RuleViolationClearedNotificationContent) notificationContent);
        } else if (NotificationType.VULNERABILITY.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class);
            notificationGroup = NotificationContentDetailOld.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (VulnerabilityNotificationContent) notificationContent);
        }

        return new NotificationDetailResultOld(notificationContent, view.getContentType(), view.getCreatedAt(), view.getType(), notificationGroup, Optional.empty(), notificationContentDetails);
    }

    public void populateContentDetails(final List<NotificationContentDetailOld> notificationContentDetails, final String notificationGroup, final PolicyOverrideNotificationContent content) {
        for (final PolicyInfo policyInfo : content.policyInfos) {
            String componentValue;
            if (content.componentVersion != null) {
                componentValue = null;
            } else {
                componentValue = content.component;
            }
            final NotificationContentDetailOld detail = NotificationContentDetailOld.createDetail(notificationGroup,
                    Optional.of(content.projectName),
                    Optional.of(content.projectVersionName),
                    Optional.of(content.projectVersion),
                    Optional.of(content.componentName),
                    Optional.ofNullable(componentValue),
                    Optional.ofNullable(content.componentVersionName),
                    Optional.ofNullable(content.componentVersion),
                    Optional.of(policyInfo.policyName),
                    Optional.of(policyInfo.policy),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());
            notificationContentDetails.add(detail);
        }
    }

    public void populateContentDetails(final List<NotificationContentDetailOld> notificationContentDetails, final String notificationGroup, final RuleViolationNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        for (final ComponentVersionStatus componentVersionStatus : content.componentVersionStatuses) {
            for (final String policyUri : componentVersionStatus.policies) {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                final NotificationContentDetailOld detail = NotificationContentDetailOld.createDetail(notificationGroup,
                        Optional.of(content.projectName),
                        Optional.of(content.projectVersionName),
                        Optional.of(content.projectVersion),
                        Optional.of(componentVersionStatus.componentName),
                        Optional.ofNullable(componentValue),
                        Optional.ofNullable(componentVersionStatus.componentVersionName),
                        Optional.ofNullable(componentVersionStatus.componentVersion),
                        Optional.of(policyName),
                        Optional.of(policyUri),
                        Optional.empty(),
                        Optional.ofNullable(componentVersionStatus.componentIssueLink),
                        Optional.empty());
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<NotificationContentDetailOld> notificationContentDetails, final String notificationGroup, final RuleViolationClearedNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        for (final ComponentVersionStatus componentVersionStatus : content.componentVersionStatuses) {
            for (final String policyUri : componentVersionStatus.policies) {
                final String policyName = uriToName.get(policyUri);
                String componentValue;
                if (componentVersionStatus.componentVersion != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.component;
                }
                final NotificationContentDetailOld detail = NotificationContentDetailOld.createDetail(notificationGroup,
                        Optional.of(content.projectName),
                        Optional.of(content.projectVersionName),
                        Optional.of(content.projectVersion),
                        Optional.of(componentVersionStatus.componentName),
                        Optional.ofNullable(componentValue),
                        Optional.ofNullable(componentVersionStatus.componentVersionName),
                        Optional.ofNullable(componentVersionStatus.componentVersion),
                        Optional.of(policyName),
                        Optional.of(policyUri),
                        Optional.empty(),
                        Optional.of(componentVersionStatus.componentIssueLink),
                        Optional.empty());
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<NotificationContentDetailOld> notificationContentDetails, final String notificationGroup, final VulnerabilityNotificationContent content) {
        for (final AffectedProjectVersion projectVersion : content.affectedProjectVersions) {
            final NotificationContentDetailOld detail = NotificationContentDetailOld.createDetail(notificationGroup,
                    Optional.of(projectVersion.projectName),
                    Optional.of(projectVersion.projectVersionName),
                    Optional.of(projectVersion.projectVersion),
                    Optional.of(content.componentName),
                    Optional.empty(),
                    Optional.of(content.versionName),
                    Optional.of(content.componentVersion),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.ofNullable(content.componentVersionOriginName),
                    Optional.ofNullable(projectVersion.componentIssueUrl),
                    Optional.ofNullable(content.componentVersionOriginId));
            notificationContentDetails.add(detail);
        }
    }

}
