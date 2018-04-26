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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusApprovalStatusType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyStatusView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.throwaway.PolicyOverrideNotificationView;
import com.blackducksoftware.integration.hub.throwaway.ReducedNotificationView;

public class PolicyViolationOverrideTransformer extends AbstractPolicyTransformer {
    public PolicyViolationOverrideTransformer(final HubService hubService, final PolicyNotificationFilter policyFilter) {
        super(hubService, policyFilter);
    }

    @Override
    public List<NotificationContentItem> transform(final ReducedNotificationView item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();
        final ProjectVersionView releaseItem;
        final PolicyOverrideNotificationView policyOverride = (PolicyOverrideNotificationView) item;
        final String projectName = policyOverride.content.projectName;
        final List<ComponentVersionStatus> componentVersionList = new ArrayList<>();
        final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
        componentStatus.bomComponentVersionPolicyStatus = policyOverride.content.bomComponentVersionPolicyStatus;
        componentStatus.componentName = policyOverride.content.componentName;
        componentStatus.componentVersion = policyOverride.content.componentVersion;

        componentVersionList.add(componentStatus);

        try {
            releaseItem = hubService.getResponse(policyOverride.content.projectVersion, ProjectVersionView.class);
        } catch (final IntegrationException e) {
            throw new HubItemTransformException(e);
        }

        handleNotification(componentVersionList, projectName, releaseItem, item, templateData);
        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final String projectName, final ProjectVersionView releaseItem, final ReducedNotificationView item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {

        final PolicyOverrideNotificationView policyOverrideItem = (PolicyOverrideNotificationView) item;
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final PolicyOverrideNotificationView policyOverride = (PolicyOverrideNotificationView) item;
                final ProjectVersionModel projectVersion;
                try {
                    projectVersion = createFullProjectVersion(policyOverride.content.projectVersion,
                            projectName, releaseItem.versionName);
                } catch (final IntegrationException e) {
                    throw new HubItemTransformException("Error getting ProjectVersion from Hub" + e.getMessage(), e);
                }

                final String componentLink = policyOverrideItem.content.component;
                final String componentVersionLink = policyOverrideItem.content.componentVersion;
                final ComponentVersionView fullComponentVersion = getComponentVersion(componentVersionLink);

                final String bomComponentVersionPolicyStatusUrl = componentVersion.bomComponentVersionPolicyStatus;
                if (StringUtils.isBlank(bomComponentVersionPolicyStatusUrl)) {
                    logger.warn(String.format("bomComponentVersionPolicyStatus is missing for component %s; skipping it",
                            componentVersion.componentName));
                    continue;
                }
                final PolicyStatusView bomComponentVersionPolicyStatus = getBomComponentVersionPolicyStatus(
                        bomComponentVersionPolicyStatusUrl);
                if (bomComponentVersionPolicyStatus.approvalStatus != PolicyStatusApprovalStatusType.IN_VIOLATION_OVERRIDDEN) {
                    logger.debug(String.format("Component %s status is not 'violation overridden'; skipping it", componentVersion.componentName));
                    continue;
                }
                final List<String> ruleList = getMatchingRuleUrls(policyOverrideItem.content.policies);
                if (ruleList != null && !ruleList.isEmpty()) {
                    final List<PolicyRuleView> policyRuleList = new ArrayList<>();
                    for (final String ruleUrl : ruleList) {
                        final PolicyRuleView rule = getPolicyRule(ruleUrl);
                        policyRuleList.add(rule);
                    }
                    createContents(projectVersion, componentVersion.componentName, fullComponentVersion,
                            componentLink, componentVersionLink, policyRuleList, item, templateData, componentVersion.componentIssueLink);
                }
            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    @Override
    public void createContents(final ProjectVersionModel projectVersion, final String componentName,
            final ComponentVersionView componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRuleView> policyRuleList, final ReducedNotificationView item,
            final List<NotificationContentItem> templateData, final String componentIssueUrl) throws URISyntaxException {
        final PolicyOverrideNotificationView policyOverride = (PolicyOverrideNotificationView) item;

        templateData.add(new PolicyOverrideContentItem(item.createdAt, projectVersion, componentName,
                componentVersion, componentUrl, componentVersionUrl, policyRuleList,
                policyOverride.content.firstName, policyOverride.content.lastName, componentIssueUrl));
    }
}
