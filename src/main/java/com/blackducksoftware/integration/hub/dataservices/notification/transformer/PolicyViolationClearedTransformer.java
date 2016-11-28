/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservices.notification.transformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationClearedTransformer extends AbstractPolicyTransformer {
    public PolicyViolationClearedTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService,
            final ComponentVersionRequestService componentVersionService, final PolicyNotificationFilter policyFilter) {
        super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
                componentVersionService, policyFilter);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();

        final RuleViolationClearedNotificationItem policyViolation = (RuleViolationClearedNotificationItem) item;
        final String projectName = policyViolation.getContent().getProjectName();
        final List<ComponentVersionStatus> componentVersionList = policyViolation.getContent()
                .getComponentVersionStatuses();
        final String projectVersionLink = policyViolation.getContent().getProjectVersionLink();
        ProjectVersionItem releaseItem;
        try {
            releaseItem = getReleaseItem(projectVersionLink);
        } catch (IOException | BDRestException | URISyntaxException e1) {
            throw new HubItemTransformException("Error getting release item while transforming notification " + item
                    + "; projectVersionLink: " + projectVersionLink + ": " + e1.getMessage(), e1);
        }
        final ProjectVersion projectVersion = new ProjectVersion();
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
            final ProjectVersion projectVersion, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final String componentVersionLink = componentVersion.getComponentVersionLink();
                final String componentVersionName = getComponentVersionName(componentVersionLink);
                final List<String> policyUrls = componentVersion.getPolicies();

                if (policyUrls != null) {
                    List<PolicyRule> ruleList = getRulesFromUrls(policyUrls);

                    ruleList = getMatchingRules(ruleList);
                    if (ruleList != null && !ruleList.isEmpty()) {
                        final List<PolicyRule> policyRuleList = new ArrayList<>();
                        for (final PolicyRule rule : ruleList) {
                            policyRuleList.add(rule);
                        }
                        createContents(projectVersion, componentVersion.getComponentName(), componentVersionName,
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

    private ProjectVersionItem getReleaseItem(final String projectVersionLink)
            throws IOException, BDRestException, URISyntaxException {
        ProjectVersionItem releaseItem = getProjectVersionService().getItem(projectVersionLink);
        return releaseItem;
    }

    @Override
    public void createContents(final ProjectVersion projectVersion, final String componentName,
            final String componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRule> policyRuleList, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        final PolicyViolationClearedContentItem contentItem = new PolicyViolationClearedContentItem(item.getCreatedAt(),
                projectVersion, componentName, componentVersion, componentUrl,
                componentVersionUrl,
                policyRuleList);
        templateData.add(contentItem);
    }

}
