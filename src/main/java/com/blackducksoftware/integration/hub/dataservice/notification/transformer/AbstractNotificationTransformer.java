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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaUtility;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.dataservice.ItemTransform;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.NotificationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public abstract class AbstractNotificationTransformer implements ItemTransform<List<NotificationContentItem>, NotificationView> {
    private final IntLogger logger;
    private final HubService hubResponseService;
    private final NotificationService notificationService;
    private final ProjectVersionService projectVersionService;
    private final PolicyService policyService;
    private final MetaUtility metaService;

    public AbstractNotificationTransformer(final HubService hubResponseService, final NotificationService notificationService, final ProjectVersionService projectVersionService,
            final PolicyService policyService, final MetaUtility metaService) {
        this.hubResponseService = hubResponseService;
        this.logger = new IntBufferedLogger();
        this.notificationService = notificationService;
        this.projectVersionService = projectVersionService;
        this.policyService = policyService;
        this.metaService = metaService;
    }

    public AbstractNotificationTransformer(final HubService hubResponseService, final IntLogger logger, final NotificationService notificationService, final ProjectVersionService projectVersionService,
            final PolicyService policyService, final MetaUtility metaService) {
        this.hubResponseService = hubResponseService;
        this.logger = logger;
        this.notificationService = notificationService;
        this.projectVersionService = projectVersionService;
        this.policyService = policyService;
        this.metaService = metaService;
    }

    public HubService getHubResponseService() {
        return hubResponseService;
    }

    protected IntLogger getLogger() {
        return logger;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public ProjectVersionService getProjectVersionService() {
        return projectVersionService;
    }

    public PolicyService getPolicyService() {
        return policyService;
    }

    @Override
    public abstract List<NotificationContentItem> transform(NotificationView item) throws HubItemTransformException;

    protected ProjectVersionModel createFullProjectVersion(final String projectVersionUrl, final String projectName, final String versionName) throws IntegrationException {
        ProjectVersionView item;
        try {
            item = hubResponseService.getView(projectVersionUrl, ProjectVersionView.class);
        } catch (final HubIntegrationException e) {
            final String msg = "Error getting the full ProjectVersion for this affected project version URL: " + projectVersionUrl + ": " + e.getMessage();
            throw new HubIntegrationException(msg, e);
        }
        final ProjectVersionModel fullProjectVersion = new ProjectVersionModel();
        fullProjectVersion.setProjectName(projectName);
        fullProjectVersion.setProjectVersionName(versionName);
        fullProjectVersion.setDistribution(item.distribution);
        fullProjectVersion.setLicense(item.license);
        fullProjectVersion.setNickname(item.nickname);
        fullProjectVersion.setPhase(item.phase);
        fullProjectVersion.setReleaseComments(item.releaseComments);
        fullProjectVersion.setReleasedOn(item.releasedOn);
        fullProjectVersion.setSource(item.source);

        fullProjectVersion.setUrl(metaService.getHref(item));
        fullProjectVersion.setCodeLocationsLink((metaService.getFirstLinkSafely(item, MetaUtility.CODE_LOCATION_LINK)));
        fullProjectVersion.setComponentsLink((metaService.getFirstLinkSafely(item, MetaUtility.COMPONENTS_LINK)));
        fullProjectVersion.setPolicyStatusLink((metaService.getFirstLinkSafely(item, MetaUtility.POLICY_STATUS_LINK)));
        fullProjectVersion.setProjectLink((metaService.getFirstLinkSafely(item, MetaUtility.PROJECT_LINK)));
        fullProjectVersion.setRiskProfileLink((metaService.getFirstLinkSafely(item, MetaUtility.RISK_PROFILE_LINK)));
        fullProjectVersion.setVersionReportLink((metaService.getFirstLinkSafely(item, MetaUtility.VERSION_REPORT_LINK)));
        fullProjectVersion.setVulnerableComponentsLink((metaService.getFirstLinkSafely(item, MetaUtility.VULNERABLE_COMPONENTS_LINK)));
        return fullProjectVersion;
    }

    public MetaUtility getMetaService() {
        return metaService;
    }

    protected ComponentVersionView getComponentVersion(final String componentVersionLink) throws IntegrationException {
        ComponentVersionView componentVersion = null;
        if (!StringUtils.isBlank(componentVersionLink)) {
            componentVersion = hubResponseService.getView(componentVersionLink, ComponentVersionView.class);
        }
        return componentVersion;
    }

    protected String getComponentVersionName(final String componentVersionLink) throws IntegrationException {
        String componentVersionName = "";
        if (!StringUtils.isBlank(componentVersionLink)) {
            final ComponentVersionView compVersion = getComponentVersion(componentVersionLink);
            if (compVersion != null) {
                componentVersionName = compVersion.versionName;
            }
        }
        return componentVersionName;
    }
}
