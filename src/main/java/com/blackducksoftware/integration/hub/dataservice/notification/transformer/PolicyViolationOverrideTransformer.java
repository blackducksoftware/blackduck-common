/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice.notification.transformer;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationOverrideTransformer extends AbstractPolicyTransformer {
    public PolicyViolationOverrideTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService,
            final HubRequestService hubRequestService, final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
                hubRequestService, policyFilter, metaService);
    }

    public PolicyViolationOverrideTransformer(final IntLogger logger,
            final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService,
            final HubRequestService hubRequestService, final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(logger, notificationService, projectVersionService, policyService, bomVersionPolicyService,
                hubRequestService, policyFilter, metaService);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();
        final ProjectVersionItem releaseItem;
        final PolicyOverrideNotificationItem policyOverride = (PolicyOverrideNotificationItem) item;
        final String projectName = policyOverride.getContent().getProjectName();
        final List<ComponentVersionStatus> componentVersionList = new ArrayList<>();
        final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
        componentStatus.setBomComponentVersionPolicyStatusLink(
                policyOverride.getContent()
                        .getBomComponentVersionPolicyStatusLink());
        componentStatus.setComponentName(policyOverride.getContent().getComponentName());
        componentStatus.setComponentVersionLink(policyOverride.getContent().getComponentVersionLink());

        componentVersionList.add(componentStatus);

        try {
            releaseItem = getProjectVersionService().getItem(policyOverride.getContent().getProjectVersionLink());
        } catch (final HubIntegrationException e) {
            throw new HubItemTransformException(e);
        }

        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(releaseItem.getVersionName());
        projectVersion.setUrl(policyOverride.getContent().getProjectVersionLink());

        handleNotification(componentVersionList, projectVersion, item, templateData);
        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final ProjectVersion projectVersion, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {

        final PolicyOverrideNotificationItem policyOverrideItem = (PolicyOverrideNotificationItem) item;
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final String componentLink = policyOverrideItem.getContent().getComponentLink();
                final String componentVersionLink = policyOverrideItem.getContent().getComponentVersionLink();
                final ComponentVersion fullComponentVersion = getComponentVersion(componentVersionLink);

                final String bomComponentVersionPolicyStatusUrl = componentVersion.getBomComponentVersionPolicyStatusLink();
                if (StringUtils.isBlank(bomComponentVersionPolicyStatusUrl)) {
                    getLogger().warn(String.format("bomComponentVersionPolicyStatus is missing for component %s; skipping it",
                            componentVersion.getComponentName()));
                    continue;
                }
                final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = getBomComponentVersionPolicyStatus(
                        bomComponentVersionPolicyStatusUrl);
                if (bomComponentVersionPolicyStatus.getApprovalStatus() != PolicyStatusEnum.IN_VIOLATION_OVERRIDDEN) {
                    getLogger().debug(String.format("Component %s status is not 'violation overridden'; skipping it", componentVersion.getComponentName()));
                    continue;
                }
                final List<String> ruleList = getMatchingRuleUrls(policyOverrideItem.getContent().getPolicies());
                if (ruleList != null && !ruleList.isEmpty()) {
                    final List<PolicyRule> policyRuleList = new ArrayList<>();
                    for (final String ruleUrl : ruleList) {
                        final PolicyRule rule = getPolicyRule(ruleUrl);
                        policyRuleList.add(rule);
                    }
                    createContents(projectVersion, componentVersion.getComponentName(), fullComponentVersion,
                            componentLink, componentVersionLink, policyRuleList, item, templateData);
                }
            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    @Override
    public void createContents(final ProjectVersion projectVersion, final String componentName,
            final ComponentVersion componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRule> policyRuleList, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        final PolicyOverrideNotificationItem policyOverride = (PolicyOverrideNotificationItem) item;

        templateData.add(new PolicyOverrideContentItem(item.getCreatedAt(), projectVersion, componentName,
                componentVersion, componentUrl, componentVersionUrl, policyRuleList,
                policyOverride.getContent().getFirstName(), policyOverride.getContent().getLastName()));
    }

}
