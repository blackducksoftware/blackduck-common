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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.dataservice.HubDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.model.ItemTransform;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionModel;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.IntLogger;

public abstract class AbstractNotificationTransformer implements ItemTransform<List<NotificationContentItem>, NotificationView> {
    private final IntLogger logger;
    private final HubDataService hubResponseService;
    private final MetaHandler metaHandler;

    public AbstractNotificationTransformer(final HubDataService hubResponseService,
            final MetaHandler metaHandler) {
        this.hubResponseService = hubResponseService;
        this.logger = new IntBufferedLogger();
        this.metaHandler = metaHandler;
    }

    public AbstractNotificationTransformer(final HubDataService hubResponseService, final IntLogger logger,
            final MetaHandler metaHandler) {
        this.hubResponseService = hubResponseService;
        this.logger = logger;
        this.metaHandler = metaHandler;
    }

    public HubDataService getHubDataService() {
        return hubResponseService;
    }

    protected IntLogger getLogger() {
        return logger;
    }

    public MetaHandler getMetaHandler() {
        return metaHandler;
    }

    @Override
    public abstract List<NotificationContentItem> transform(NotificationView item) throws HubItemTransformException;

    protected ProjectVersionModel createFullProjectVersion(final String projectVersionUrl, final String projectName, final String versionName) throws IntegrationException {
        ProjectVersionView item;
        try {
            item = hubResponseService.getResponse(projectVersionUrl, ProjectVersionView.class);
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

        fullProjectVersion.setUrl(metaHandler.getHref(item));
        fullProjectVersion.setCodeLocationsLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.CODELOCATIONS_LINK)));
        fullProjectVersion.setComponentsLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.COMPONENTS_LINK)));
        fullProjectVersion.setPolicyStatusLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.POLICY_STATUS_LINK)));
        fullProjectVersion.setProjectLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.PROJECT_LINK)));
        fullProjectVersion.setRiskProfileLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.RISKPROFILE_LINK)));
        fullProjectVersion.setVersionReportLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.VERSIONREPORT_LINK)));
        fullProjectVersion.setVulnerableComponentsLink((metaHandler.getFirstLinkSafely(item, ProjectVersionView.VULNERABLE_COMPONENTS_LINK)));
        return fullProjectVersion;
    }

    protected ComponentVersionView getComponentVersion(final String componentVersionLink) throws IntegrationException {
        ComponentVersionView componentVersion = null;
        if (!StringUtils.isBlank(componentVersionLink)) {
            componentVersion = hubResponseService.getResponse(componentVersionLink, ComponentVersionView.class);
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
