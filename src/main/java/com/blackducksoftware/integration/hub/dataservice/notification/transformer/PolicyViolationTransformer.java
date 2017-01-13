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

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public class PolicyViolationTransformer extends AbstractPolicyTransformer {
    public PolicyViolationTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService,
            final HubRequestService hubRequestService, final PolicyNotificationFilter policyFilter, final MetaService metaService) {
        super(notificationService, projectVersionService, policyService, bomVersionPolicyService,
                hubRequestService, policyFilter, metaService);
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
        } catch (final HubIntegrationException e) {
            throw new HubItemTransformException(e);
        }
        ProjectVersion projectVersion;
        try {
            projectVersion = createFullProjectVersion(policyViolation.getContent().getProjectVersionLink(),
                    projectName, releaseItem.getVersionName());
        } catch (final HubIntegrationException e) {
            throw new HubItemTransformException("Error getting ProjectVersion from Hub" + e.getMessage(), e);
        }

        handleNotification(componentVersionList, projectVersion, item, templateData);

        return templateData;
    }

    @Override
    public void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final ProjectVersion projectVersion, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException {
        handleNotificationUsingBomComponentVersionPolicyStatusLink(componentVersionList, projectVersion, item,
                templateData);
    }

    private ProjectVersionItem getReleaseItem(final String projectVersionLink) throws HubIntegrationException {
        final ProjectVersionItem releaseItem = getProjectVersionService().getItem(projectVersionLink);
        return releaseItem;
    }

    @Override
    public void createContents(final ProjectVersion projectVersion, final String componentName,
            final String componentVersion, final String componentUrl, final String componentVersionUrl,
            final List<PolicyRule> policyRuleList, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws URISyntaxException {
        templateData.add(new PolicyViolationContentItem(item.getCreatedAt(), projectVersion, componentName,
                componentVersion, componentUrl, componentVersionUrl, policyRuleList));
    }

}
