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
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;

public class NotificationContentDetailFactory {
    public List<NotificationContentDetail> generateNotificationContentDetails(final CommonNotificationState commonNotificationState) {
        return generateNotificationContentDetails(commonNotificationState.getType(), commonNotificationState.getContent());
    }

    public List<NotificationContentDetail> generateNotificationContentDetails(final NotificationType notificationType, final NotificationContent notificationContent) {
        List<NotificationContentDetail> detailsList;
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
            detailsList = generatePolicyOverrideContentDetails((PolicyOverrideNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            detailsList = generateRuleViolationContentDetails((RuleViolationNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            detailsList = generateRuleViolationClearedContentDetails((RuleViolationClearedNotificationContent) notificationContent);
        } else if (NotificationType.VULNERABILITY.equals(notificationType)) {
            detailsList = generateVulnerabilityContentDetails((VulnerabilityNotificationContent) notificationContent);
        } else {
            detailsList = Collections.emptyList();
        }
        return detailsList;
    }

    public List<NotificationContentDetail> generatePolicyOverrideContentDetails(final PolicyOverrideNotificationContent content) {
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

    public List<NotificationContentDetail> generateRuleViolationContentDetails(final RuleViolationNotificationContent content) {
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

    public List<NotificationContentDetail> generateRuleViolationClearedContentDetails(final RuleViolationClearedNotificationContent content) {
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

    public List<NotificationContentDetail> generateVulnerabilityContentDetails(final VulnerabilityNotificationContent content) {
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.affectedProjectVersions.forEach(projectVersion -> {
            details.add(NotificationContentDetail.createDetail(content, NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY, projectVersion.projectName, projectVersion.projectVersionName, projectVersion.projectVersion,
                    content.componentName, null, content.versionName, content.componentVersion, null, null, content.componentVersionOriginName, projectVersion.componentIssueUrl, content.componentVersionOriginId));
        });
        return details;
    }

}
