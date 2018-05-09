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
import java.util.Map;
import java.util.stream.Collectors;

public class RuleViolationClearedNotificationContent extends NotificationContent {
    public String projectName;
    public String projectVersionName;
    public String projectVersion;
    public int componentVersionsCleared;
    public List<ComponentVersionStatus> componentVersionStatuses;
    public List<PolicyInfo> policyInfos;

    @Override
    public boolean providesPolicyDetails() {
        return true;
    }

    @Override
    public boolean providesVulnerabilityDetails() {
        return false;
    }

    @Override
    public boolean providesProjectComponentDetails() {
        return true;
    }

    @Override
    public boolean providesLicenseDetails() {
        return false;
    }

    @Override
    public List<NotificationContentDetail> getNotificationContentDetails() {
        final Map<String, String> uriToName = policyInfos.stream().collect(Collectors.toMap(policyInfo -> policyInfo.policy, policyInfo -> policyInfo.policyName));
        final List<NotificationContentDetail> details = new ArrayList<>();
        componentVersionStatuses.forEach(componentVersionStatus -> {
            componentVersionStatus.policies.forEach(policyUri -> {
                final String policyName = uriToName.get(policyUri);
                if (componentVersionStatus.componentVersion != null) {
                    details.add(
                            NotificationContentDetail.createPolicyDetailWithComponentVersionAndIssue(this, projectName, projectVersionName, projectVersion, componentVersionStatus.componentName, componentVersionStatus.componentVersionName,
                                    componentVersionStatus.componentVersion, policyName, policyUri, componentVersionStatus.componentIssueLink));
                } else {
                    details.add(NotificationContentDetail.createPolicyDetailWithComponentAndIssue(this, projectName, projectVersionName, projectVersion, componentVersionStatus.componentName, componentVersionStatus.component, policyName,
                            policyUri, componentVersionStatus.componentIssueLink));
                }
            });
        });
        return details;
    }

    @Override
    public String getNotificationGroup() {
        return NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
    }

}
