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
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.ReducedNotificationView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.parallel.processor.ItemTransformer;

public abstract class AbstractNotificationTransformer implements ItemTransformer<NotificationContentItem, ReducedNotificationView> {
    final IntLogger logger;
    final HubService hubService;

    public AbstractNotificationTransformer(final HubService hubService) {
        this.hubService = hubService;
        this.logger = hubService.getRestConnection().logger;
    }

    @Override
    public abstract List<NotificationContentItem> transform(ReducedNotificationView item) throws HubItemTransformException;

    protected ProjectVersionModel createFullProjectVersion(final String projectVersionUrl, final String projectName, final String versionName) throws IntegrationException {
        ProjectVersionView item;
        try {
            item = hubService.getResponse(projectVersionUrl, ProjectVersionView.class);
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

        fullProjectVersion.setUrl(hubService.getHref(item));
        fullProjectVersion.setCodeLocationsLink((hubService.getFirstLinkSafely(item, ProjectVersionView.CODELOCATIONS_LINK)));
        fullProjectVersion.setComponentsLink((hubService.getFirstLinkSafely(item, ProjectVersionView.COMPONENTS_LINK)));
        fullProjectVersion.setPolicyStatusLink((hubService.getFirstLinkSafely(item, ProjectVersionView.POLICY_STATUS_LINK)));
        fullProjectVersion.setProjectLink((hubService.getFirstLinkSafely(item, ProjectVersionView.PROJECT_LINK)));
        fullProjectVersion.setRiskProfileLink((hubService.getFirstLinkSafely(item, ProjectVersionView.RISKPROFILE_LINK)));
        fullProjectVersion.setVersionReportLink((hubService.getFirstLinkSafely(item, ProjectVersionView.VERSIONREPORT_LINK)));
        fullProjectVersion.setVulnerableComponentsLink((hubService.getFirstLinkSafely(item, ProjectVersionView.VULNERABLE_COMPONENTS_LINK)));
        return fullProjectVersion;
    }

    protected ComponentVersionView getComponentVersion(final String componentVersionLink) throws IntegrationException {
        ComponentVersionView componentVersion = null;
        if (!StringUtils.isBlank(componentVersionLink)) {
            componentVersion = hubService.getResponse(componentVersionLink, ComponentVersionView.class);
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
