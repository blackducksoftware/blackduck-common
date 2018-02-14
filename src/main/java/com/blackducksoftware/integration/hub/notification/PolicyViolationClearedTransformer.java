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
package com.blackducksoftware.integration.hub.notification;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.response.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.view.RuleViolationClearedNotificationView;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class PolicyViolationClearedTransformer extends AbstractPolicyTransformer {
    public PolicyViolationClearedTransformer(final HubService hubResponseService,
            final PolicyNotificationFilter policyFilter, final MetaHandler metaHandler) {
        super(hubResponseService,
                policyFilter, metaHandler);
    }

    public PolicyViolationClearedTransformer(final HubService hubResponseService, final IntLogger logger,
            final PolicyNotificationFilter policyFilter, final MetaHandler metaHandler) {
        super(hubResponseService, logger,
                policyFilter, metaHandler);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationView item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();

        final RuleViolationClearedNotificationView policyViolation = (RuleViolationClearedNotificationView) item;
        final String projectName = policyViolation.content.projectName;
        final List<ComponentVersionStatus> componentVersionList = policyViolation.content.componentVersionStatuses;
        final String projectVersionLink = policyViolation.content.projectVersionLink;
        ProjectVersionView releaseItem;
        try {
            releaseItem = getHubDataService().getResponse(projectVersionLink, ProjectVersionView.class);
        } catch (final IntegrationException e1) {
            throw new HubItemTransformException("Error getting release item while transforming notification " + item
                    + "; projectVersionLink: " + projectVersionLink + ": " + e1.getMessage(), e1);
        }

        try {
            handleNotification(componentVersionList, projectName, releaseItem, item, templateData);
        } catch (final HubItemTransformException e) {
            throw new HubItemTransformException("Error in handleNotification() while transforming notification " + item
                    + "; projectVersionLink: " + projectVersionLink + ": " + e.getMessage(), e);
        }

        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final String projectName, final ProjectVersionView releaseItem, final NotificationView item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final RuleViolationClearedNotificationView policyViolation = (RuleViolationClearedNotificationView) item;
                final ProjectVersionModel projectVersion;
                try {
                    projectVersion = createFullProjectVersion(policyViolation.content.projectVersionLink,
                            projectName, releaseItem.versionName);
                } catch (final IntegrationException e) {
                    throw new HubItemTransformException("Error getting ProjectVersion from Hub" + e.getMessage(), e);
                }

                final String componentVersionLink = componentVersion.componentVersionLink;
                final ComponentVersionView fullComponentVersion = getComponentVersion(componentVersionLink);
                final List<String> policyUrls = getMatchingRuleUrls(componentVersion.policies);

                if (policyUrls != null) {
                    List<PolicyRuleView> ruleList = getRulesFromUrls(policyUrls);

                    ruleList = getMatchingRules(ruleList);
                    if (ruleList != null && !ruleList.isEmpty()) {
                        final List<PolicyRuleView> policyRuleList = new ArrayList<>();
                        for (final PolicyRuleView rule : ruleList) {
                            policyRuleList.add(rule);
                        }
                        createContents(projectVersion, componentVersion.componentName, fullComponentVersion,
                                componentVersion.componentLink,
                                componentVersion.componentVersionLink,
                                policyRuleList, item, templateData, componentVersion.componentIssueLink);
                    }
                }
            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    @Override
    public void createContents(final ProjectVersionModel projectVersion, final String componentName,
            final ComponentVersionView componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRuleView> policyRuleList, final NotificationView item,
            final List<NotificationContentItem> templateData, final String componentIssueUrl) throws URISyntaxException {
        final PolicyViolationClearedContentItem contentItem = new PolicyViolationClearedContentItem(item.createdAt,
                projectVersion, componentName, componentVersion, componentUrl,
                componentVersionUrl,
                policyRuleList, componentIssueUrl);
        templateData.add(contentItem);
    }

}
