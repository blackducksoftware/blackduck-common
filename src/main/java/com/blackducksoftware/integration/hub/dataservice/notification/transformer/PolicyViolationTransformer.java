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
package com.blackducksoftware.integration.hub.dataservice.notification.transformer;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationTransformer extends AbstractPolicyTransformer {
    public PolicyViolationTransformer(final HubService hubResponseService, final NotificationService notificationService, final ProjectVersionService projectVersionService, final PolicyService policyService,
            final PolicyNotificationFilter policyFilter, final MetaHandler metaService) {
        super(hubResponseService, notificationService, projectVersionService, policyService, policyFilter, metaService);
    }

    public PolicyViolationTransformer(final HubService hubResponseService, final IntLogger logger, final NotificationService notificationService, final ProjectVersionService projectVersionService,
            final PolicyService policyService, final PolicyNotificationFilter policyFilter, final MetaHandler metaService) {
        super(hubResponseService, logger, notificationService, projectVersionService, policyService, policyFilter, metaService);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationView item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();
        final RuleViolationNotificationView policyViolation = (RuleViolationNotificationView) item;
        final String projectName = policyViolation.content.projectName;
        final List<ComponentVersionStatus> componentVersionList = policyViolation.content.componentVersionStatuses;
        final String projectVersionLink = policyViolation.content.projectVersionLink;
        ProjectVersionView releaseItem;
        try {
            releaseItem = getReleaseItem(projectVersionLink);
        } catch (final IntegrationException e) {
            throw new HubItemTransformException(e);
        }

        handleNotification(componentVersionList, projectName, releaseItem, item, templateData);

        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList, final String projectName, final ProjectVersionView releaseItem, final NotificationView item, final List<NotificationContentItem> templateData)
            throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final RuleViolationNotificationView policyViolation = (RuleViolationNotificationView) item;
                ProjectVersionModel projectVersion;
                try {
                    projectVersion = createFullProjectVersion(policyViolation.content.projectVersionLink, projectName, releaseItem.versionName);
                } catch (final IntegrationException e) {
                    throw new HubItemTransformException("Error getting ProjectVersion from Hub" + e.getMessage(), e);
                }

                final String componentVersionLink = componentVersion.componentVersionLink;
                final ComponentVersionView fullComponentVersion = getComponentVersion(componentVersionLink);
                if ((componentVersion.policies == null) || (componentVersion.policies.size() == 0)) {
                    throw new HubItemTransformException("The polices list in the component version status is null or empty");
                }
                final List<String> ruleList = getMatchingRuleUrls(componentVersion.policies);
                if (ruleList != null && !ruleList.isEmpty()) {
                    final List<PolicyRuleView> policyRuleList = new ArrayList<>();
                    for (final String ruleUrl : ruleList) {
                        final PolicyRuleView rule = getPolicyRule(ruleUrl);
                        policyRuleList.add(rule);
                    }
                    createContents(projectVersion, componentVersion.componentName, fullComponentVersion, componentVersion.componentLink, componentVersion.componentVersionLink, policyRuleList, item, templateData,
                            componentVersion.componentIssueLink);
                }

            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    private ProjectVersionView getReleaseItem(final String projectVersionLink) throws IntegrationException {
        final ProjectVersionView releaseItem = getProjectVersionService().getResponse(projectVersionLink, ProjectVersionView.class);
        return releaseItem;
    }

    @Override
    public void createContents(final ProjectVersionModel projectVersion, final String componentName, final ComponentVersionView componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRuleView> policyRuleList, final NotificationView item, final List<NotificationContentItem> templateData, final String componentIssueUrl) throws URISyntaxException {
        templateData.add(new PolicyViolationContentItem(item.createdAt, projectVersion, componentName, componentVersion, componentUrl, componentVersionUrl, policyRuleList, componentIssueUrl));
    }
}
