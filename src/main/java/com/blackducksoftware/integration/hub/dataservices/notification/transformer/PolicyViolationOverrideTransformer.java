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

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public class PolicyViolationOverrideTransformer extends AbstractPolicyTransformer {
    public PolicyViolationOverrideTransformer(final NotificationRestService notificationService,
            final ReleaseItemRestService projectVersionService, final PolicyRestService policyService,
            final VersionBomPolicyRestService bomVersionPolicyService,
            final ComponentVersionRestService componentVersionService, final PolicyNotificationFilter policyFilter) {
        super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
                componentVersionService, policyFilter);
    }

    @Override
    public List<NotificationContentItem> transform(final NotificationItem item) throws HubItemTransformException {
        final List<NotificationContentItem> templateData = new ArrayList<>();
        try {
            final ReleaseItem releaseItem;
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

            releaseItem = getProjectVersionService().getItem(policyOverride.getContent().getProjectVersionLink());

            final ProjectVersion projectVersion = new ProjectVersion();
            projectVersion.setProjectName(projectName);
            projectVersion.setProjectVersionName(releaseItem.getVersionName());
            projectVersion.setUrl(policyOverride.getContent().getProjectVersionLink());

            handleNotification(componentVersionList, projectVersion, item, templateData);
        } catch (IOException | BDRestException | URISyntaxException e) {
            throw new HubItemTransformException(e);
        }
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
                String componentVersionName = null;
                if (componentVersionLink != null) {
                    componentVersionName = getComponentVersionName(componentVersionLink);
                }
                final String policyStatusUrl = componentVersion.getBomComponentVersionPolicyStatusLink();

                if (StringUtils.isNotBlank(policyStatusUrl)) {
                    final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = getBomComponentVersionPolicyStatus(policyStatusUrl);
                    List<String> ruleList = getRuleUrls(bomComponentVersionPolicyStatus
                            .getLinks(BomComponentVersionPolicyStatus.POLICY_RULE_URL));

                    ruleList = getMatchingRuleUrls(ruleList);
                    if (ruleList != null && !ruleList.isEmpty()) {
                        final List<PolicyRule> policyRuleList = new ArrayList<>();
                        for (final String ruleUrl : ruleList) {
                            final PolicyRule rule = getPolicyRule(ruleUrl);
                            policyRuleList.add(rule);
                        }
                        createContents(projectVersion, componentVersion.getComponentName(), componentVersionName,
                                componentLink, componentVersionLink, policyRuleList, item, templateData);
                    }
                }
            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    @Override
    public void createContents(final ProjectVersion projectVersion, final String componentName,
            final String componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRule> policyRuleList, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        final PolicyOverrideNotificationItem policyOverride = (PolicyOverrideNotificationItem) item;

        templateData.add(new PolicyOverrideContentItem(item.getCreatedAt(), projectVersion, componentName,
                componentVersion, componentUrl, componentVersionUrl, policyRuleList,
                policyOverride.getContent().getFirstName(), policyOverride.getContent().getLastName()));
    }

}
