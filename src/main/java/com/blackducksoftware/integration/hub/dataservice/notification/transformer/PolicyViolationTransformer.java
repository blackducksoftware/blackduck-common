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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.model.type.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationTransformer extends AbstractPolicyTransformer {
    public PolicyViolationTransformer(final HubResponseService hubResponseService, final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(hubResponseService, notificationService, projectVersionService, policyService,
                policyFilter, metaService);
    }

    public PolicyViolationTransformer(final HubResponseService hubResponseService, final IntLogger logger,
            final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(hubResponseService, logger, notificationService, projectVersionService, policyService,
                policyFilter, metaService);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();
        final RuleViolationNotificationItem policyViolation = (RuleViolationNotificationItem) item;
        final String projectName = policyViolation.getContent().getProjectName();
        final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
                .getComponentVersionStatuses();
        final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
        ProjectVersionItem releaseItem;
        try {
            releaseItem = getReleaseItem(projectVersionLink);
        } catch (final IntegrationException e) {
            throw new HubItemTransformException(e);
        }
        ProjectVersionModel projectVersion;
        try {
            projectVersion = createFullProjectVersion(policyViolation.getContent().getProjectVersionLink(),
                    projectName, releaseItem.getVersionName());
        } catch (final IntegrationException e) {
            throw new HubItemTransformException("Error getting ProjectVersion from Hub" + e.getMessage(), e);
        }

        handleNotification(componentVersionList, projectVersion, item, templateData);

        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final ProjectVersionModel projectVersion, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final String bomComponentVersionPolicyStatusUrl = componentVersion.getBomComponentVersionPolicyStatusLink();
                if (StringUtils.isBlank(bomComponentVersionPolicyStatusUrl)) {
                    getLogger().warn(String.format("bomComponentVersionPolicyStatus is missing for component %s; skipping it",
                            componentVersion.getComponentName()));
                    continue;
                }
                final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = getBomComponentVersionPolicyStatus(bomComponentVersionPolicyStatusUrl);
                if (bomComponentVersionPolicyStatus.getApprovalStatus() != VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION) {
                    getLogger().debug(String.format("Component %s is not in violation; skipping it", componentVersion.getComponentName()));
                    continue;
                }

                final String componentVersionLink = componentVersion.getComponentVersionLink();
                final ComponentVersion fullComponentVersion = getComponentVersion(componentVersionLink);
                if ((componentVersion.getPolicies() == null) || (componentVersion.getPolicies().size() == 0)) {
                    throw new HubItemTransformException("The polices list in the component version status is null or empty");
                }
                final List<String> ruleList = getMatchingRuleUrls(componentVersion.getPolicies());
                if (ruleList != null && !ruleList.isEmpty()) {
                    final List<PolicyRule> policyRuleList = new ArrayList<>();
                    for (final String ruleUrl : ruleList) {
                        final PolicyRule rule = getPolicyRule(ruleUrl);
                        policyRuleList.add(rule);
                    }
                    createContents(projectVersion, componentVersion.getComponentName(), fullComponentVersion,
                            componentVersion.getComponentLink(),
                            componentVersion.getComponentVersionLink(),
                            policyRuleList, item, templateData);
                }

            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    private ProjectVersionItem getReleaseItem(final String projectVersionLink) throws IntegrationException {
        final ProjectVersionItem releaseItem = getProjectVersionService().getItem(projectVersionLink, ProjectVersionItem.class);
        return releaseItem;
    }

    @Override
    public void createContents(final ProjectVersionModel projectVersion, final String componentName,
            final ComponentVersion componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRule> policyRuleList, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        templateData.add(new PolicyViolationContentItem(item.getCreatedAt(), projectVersion, componentName,
                componentVersion, componentUrl, componentVersionUrl, policyRuleList));
    }
}
