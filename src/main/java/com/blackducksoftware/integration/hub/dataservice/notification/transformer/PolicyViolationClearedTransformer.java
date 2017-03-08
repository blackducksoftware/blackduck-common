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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.NotificationView;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.model.view.components.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationClearedTransformer extends AbstractPolicyTransformer {
    public PolicyViolationClearedTransformer(final HubResponseService hubResponseService, final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(hubResponseService, notificationService, projectVersionService, policyService,
                policyFilter, metaService);
    }

    public PolicyViolationClearedTransformer(final HubResponseService hubResponseService, final IntLogger logger,
            final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(hubResponseService, logger, notificationService, projectVersionService, policyService,
                policyFilter, metaService);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationView item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();

        final RuleViolationClearedNotificationView policyViolation = (RuleViolationClearedNotificationView) item;
        final String projectName = policyViolation.getContent().getProjectName();
        final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
                .getComponentVersionStatuses();
        final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
        ProjectVersionView releaseItem;
        try {
            releaseItem = getReleaseItem(projectVersionLink);
        } catch (final IntegrationException e1) {
            throw new HubItemTransformException("Error getting release item while transforming notification " + item
                    + "; projectVersionLink: " + projectVersionLink + ": " + e1.getMessage(), e1);
        }
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName(projectName);
        projectVersion.setProjectVersionName(releaseItem.getVersionName());
        projectVersion.setUrl(projectVersionLink);

        try {
            handleNotification(componentVersionList, projectVersion, item, templateData);
        } catch (final HubItemTransformException e) {
            throw new HubItemTransformException("Error in handleNotification() while transforming notification " + item
                    + "; projectVersionLink: " + projectVersionLink + ": " + e.getMessage(), e);
        }

        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final ProjectVersionModel projectVersion, final NotificationView item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final String componentVersionLink = componentVersion.getComponentVersionLink();
                final ComponentVersionView fullComponentVersion = getComponentVersion(componentVersionLink);
                final List<String> policyUrls = getMatchingRuleUrls(componentVersion.getPolicies());

                if (policyUrls != null) {
                    List<PolicyRuleView> ruleList = getRulesFromUrls(policyUrls);

                    ruleList = getMatchingRules(ruleList);
                    if (ruleList != null && !ruleList.isEmpty()) {
                        final List<PolicyRuleView> policyRuleList = new ArrayList<>();
                        for (final PolicyRuleView rule : ruleList) {
                            policyRuleList.add(rule);
                        }
                        createContents(projectVersion, componentVersion.getComponentName(), fullComponentVersion,
                                componentVersion.getComponentLink(),
                                componentVersion.getComponentVersionLink(),
                                policyRuleList, item, templateData);
                    }
                }
            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    private ProjectVersionView getReleaseItem(final String projectVersionLink) throws IntegrationException {
        final ProjectVersionView releaseItem = getProjectVersionService().getItem(projectVersionLink, ProjectVersionView.class);
        return releaseItem;
    }

    @Override
    public void createContents(final ProjectVersionModel projectVersion, final String componentName,
            final ComponentVersionView componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRuleView> policyRuleList, final NotificationView item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        final PolicyViolationClearedContentItem contentItem = new PolicyViolationClearedContentItem(item.getCreatedAt(),
                projectVersion, componentName, componentVersion, componentUrl,
                componentVersionUrl,
                policyRuleList);
        templateData.add(contentItem);
    }

}
