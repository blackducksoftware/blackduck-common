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
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.LicenseLimitNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
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

    public NotificationDetailResult generateContentDetails(final CommonNotificationView view) {
        final NotificationType type = view.getType();
        final String notificationJson = view.json;
        final JsonObject jsonObject = jsonParser.parse(notificationJson).getAsJsonObject();

        NotificationContent notificationContent = null;
        final List<? extends NotificationContentDetail> contentDetailList = new ArrayList<>();

        if (NotificationType.LICENSE_LIMIT.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), LicenseLimitNotificationContent.class);
            populateContentDetails((List<LicenseLimitNotificationContentDetail>) contentDetailList, (LicenseLimitNotificationContent) notificationContent);
        } else if (NotificationType.POLICY_OVERRIDE.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class);
            populateContentDetails((List<PolicyNotificationContentDetail>) contentDetailList, (PolicyOverrideNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class);
            populateContentDetails((List<PolicyNotificationContentDetail>) contentDetailList, (RuleViolationNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class);
            populateContentDetails((List<PolicyNotificationContentDetail>) contentDetailList, (RuleViolationClearedNotificationContent) notificationContent);
        } else if (NotificationType.VULNERABILITY.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class);
            populateContentDetails((List<VulnerabilityNotificationContentDetail>) contentDetailList, (VulnerabilityNotificationContent) notificationContent);
        }
        // TODO don't forget to add this for Hub 4.8.0
        // else if (NotificationType.BOM_EDIT.equals(type)) {
        // }

        // @formatter:off
        return new NotificationDetailResult(
                  contentDetailList
                 ,view.getContentType()
                 ,view.getCreatedAt()
                 ,view.getType()
                );
        // @formatter:on
    }

    public void populateContentDetails(final List<LicenseLimitNotificationContentDetail> notificationContentDetails, final LicenseLimitNotificationContent content) {
        // @formatter:off
        final LicenseLimitNotificationContentDetail detail = new LicenseLimitNotificationContentDetail(
                  content.licenseViolationType
                 ,content.message
                 ,content.marketingPageUrl
                 ,content.usedCodeSize
                 ,content.hardLimit
                 ,content.softLimit
                );
        // @formatter:on
        notificationContentDetails.add(detail);
    }

    public void populateContentDetails(final List<PolicyNotificationContentDetail> notificationContentDetails, final PolicyOverrideNotificationContent content) {
        for (final PolicyInfo policyInfo : content.policyInfos) {
            // @formatter:off
            final PolicyNotificationContentDetail detail = new PolicyNotificationContentDetail(
                      content.projectName
                     ,content.projectVersionName
                     ,content.projectVersion
                     ,content.componentName
                     ,content.component
                     ,content.componentVersionName
                     ,content.componentVersion
                     ,policyInfo.policyName
                     ,policyInfo.policy
                     ,content.firstName
                     ,content.lastName
                    );
            // @formatter:on
            notificationContentDetails.add(detail);
        }
    }

    public void populateContentDetails(final List<PolicyNotificationContentDetail> notificationContentDetails, final RuleViolationNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        for (final ComponentVersionStatus componentVersionStatus : content.componentVersionStatuses) {
            for (final String policyUri : componentVersionStatus.policies) {
                final String policyName = uriToName.get(policyUri);
                // @formatter:off
                final PolicyNotificationContentDetail detail = new PolicyNotificationContentDetail(
                          content.projectName
                         ,content.projectVersionName
                         ,content.projectVersion
                         ,componentVersionStatus.componentName
                         ,componentVersionStatus.component
                         ,componentVersionStatus.componentVersionName
                         ,componentVersionStatus.componentVersion
                         ,policyName
                         ,policyUri
                         ,null
                         ,null
                        );
                // @formatter:on
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<PolicyNotificationContentDetail> notificationContentDetails, final RuleViolationClearedNotificationContent content) {
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        for (final ComponentVersionStatus componentVersionStatus : content.componentVersionStatuses) {
            for (final String policyUri : componentVersionStatus.policies) {
                final String policyName = uriToName.get(policyUri);
                // @formatter:off
                final PolicyNotificationContentDetail detail = new PolicyNotificationContentDetail(
                          content.projectName
                         ,content.projectVersionName
                         ,content.projectVersion
                         ,componentVersionStatus.componentName
                         ,componentVersionStatus.component
                         ,componentVersionStatus.componentVersionName
                         ,componentVersionStatus.componentVersion
                         ,policyName
                         ,policyUri
                         ,null
                         ,null
                        );
                // @formatter:on
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<VulnerabilityNotificationContentDetail> notificationContentDetails, final VulnerabilityNotificationContent content) {
        for (final AffectedProjectVersion projectVersion : content.affectedProjectVersions) {
            // @formatter:off
            final VulnerabilityNotificationContentDetail detail = new VulnerabilityNotificationContentDetail(
                      projectVersion.projectName
                     ,projectVersion.projectVersionName
                     ,projectVersion.projectVersion
                     ,content.componentName
                     ,content.versionName
                     ,content.componentVersion
                     ,content.newVulnerabilityIds
                     ,content.updatedVulnerabilityIds
                     ,content.deletedVulnerabilityIds
                    );
            //  @formatter:on
            notificationContentDetails.add(detail);
        }
    }

}
