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
package com.blackducksoftware.integration.hub.notification.content.collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.LicenseLimitNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;

public class ContentDetailCollector {
    Map<Class<? extends NotificationContent>, Function<NotificationContent, List<NotificationContentDetail>>> creatorMap;

    public ContentDetailCollector() {
        creatorMap.put(RuleViolationNotificationContent.class, this::createRuleViolationDetails);
        creatorMap.put(RuleViolationClearedNotificationContent.class, this::createRuleViolationClearedDetails);
        creatorMap.put(PolicyOverrideNotificationContent.class, this::createPolicyOverrideDetails);
        creatorMap.put(VulnerabilityNotificationContent.class, this::createVulnerabilityDetails);
        creatorMap.put(LicenseLimitNotificationContent.class, this::createLicenseDetails);
    }

    public List<NotificationContentDetail> collect(final List<CommonNotificationState> commonNotificationStates) {
        if (commonNotificationStates.isEmpty()) {
            return Collections.emptyList();
        }
        final List<NotificationContentDetail> contentDetailList = new ArrayList<>(50);
        commonNotificationStates.stream().map(CommonNotificationState::getContent).forEach(content -> {
            collectDetails(contentDetailList, content);
        });

        return contentDetailList;
    }

    private void collectDetails(final List<NotificationContentDetail> contentDetailList, final NotificationContent notificationContent) {
        final Class<?> key = notificationContent.getClass();
        if (creatorMap.containsKey(key)) {
            final Function<NotificationContent, List<NotificationContentDetail>> createDetailFunction = creatorMap.get(key);
            contentDetailList.addAll(createDetailFunction.apply(notificationContent));
        }
    }

    private List<NotificationContentDetail> createRuleViolationDetails(final NotificationContent notificationContent) {
        final RuleViolationNotificationContent content = (RuleViolationNotificationContent) notificationContent;
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                details.add(NotificationContentDetail.createDetail(content, content.projectName, content.projectVersionName, content.projectVersion, componentVersionStatus.componentName,
                        componentVersionStatus.component, componentVersionStatus.componentVersionName, componentVersionStatus.componentVersion, policyName,
                        policyUri, null, componentVersionStatus.componentIssueLink, null));
            });
        });
        return details;
    }

    private List<NotificationContentDetail> createRuleViolationClearedDetails(final NotificationContent notificationContent) {
        final RuleViolationClearedNotificationContent content = (RuleViolationClearedNotificationContent) notificationContent;
        final Map<String, String> uriToName = content.policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                details.add(NotificationContentDetail.createDetail(content, content.projectName, content.projectVersionName, content.projectVersion, componentVersionStatus.componentName,
                        componentVersionStatus.component, componentVersionStatus.componentVersionName, componentVersionStatus.componentVersion, policyName,
                        policyUri, null, componentVersionStatus.componentIssueLink, null));
            });
        });
        return details;
    }

    private List<NotificationContentDetail> createPolicyOverrideDetails(final NotificationContent notificationContent) {
        final PolicyOverrideNotificationContent content = (PolicyOverrideNotificationContent) notificationContent;
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.policyInfos.forEach(policyInfo -> {
            details.add(NotificationContentDetail.createDetail(content, content.projectName, content.projectVersionName, content.projectVersion, content.componentName,
                    content.component, content.componentVersionName, content.componentVersion, policyInfo.policyName,
                    policyInfo.policy, null, null, null));
        });
        return details;
    }

    private List<NotificationContentDetail> createVulnerabilityDetails(final NotificationContent notificationContent) {
        final VulnerabilityNotificationContent content = (VulnerabilityNotificationContent) notificationContent;
        final List<NotificationContentDetail> details = new ArrayList<>();
        content.affectedProjectVersions.forEach(projectVersion -> {
            details.add(NotificationContentDetail.createDetail(content, projectVersion.projectName, projectVersion.projectVersionName, projectVersion.projectVersion, content.componentName,
                    null, content.versionName, content.componentVersion, null, null,
                    content.componentVersionOriginName, projectVersion.componentIssueUrl, content.componentVersionOriginId));
        });
        return details;
    }

    private List<NotificationContentDetail> createLicenseDetails(final NotificationContent notificationContent) {
        return Collections.emptyList();
    }
}
