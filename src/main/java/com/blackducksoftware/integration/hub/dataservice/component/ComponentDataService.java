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
package com.blackducksoftware.integration.hub.dataservice.component;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.response.ComponentSearchResultResponse;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.ComponentView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService {
    private final ProjectRequestService projectRequestService;
    private final ProjectVersionRequestService projectVersionRequestService;
    private final ComponentRequestService componentRequestService;
    private final IntLogger logger;

    public ComponentDataService(final IntLogger logger, final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService, final ComponentRequestService componentRequestService) {
        this.logger = logger;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.componentRequestService = componentRequestService;
    }

    public ComponentVersionView getExactComponentVersionFromComponent(final String namespace, final String groupId, final String artifactId, final String version) throws IntegrationException {
        for (final ComponentVersionView componentVersion : this.getAllComponentVersionsFromComponent(namespace, groupId, artifactId, version)) {
            if (componentVersion.versionName.equals(version)) {
                return componentVersion;
            }
        }
        final String errMsg = "Could not find version " + version + " of component " + StringUtils.join(new String[] { groupId, artifactId, version }, ":");
        logger.error(errMsg);
        throw new HubIntegrationException(errMsg);
    }

    public List<ComponentVersionView> getAllComponentVersionsFromComponent(final String namespace, final String groupId, final String artifactId, final String version) throws IntegrationException {
        final ComponentSearchResultResponse componentResponse = componentRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);

        final ComponentView componentView = componentRequestService.getItem(componentResponse.component, ComponentView.class);
        final List<ComponentVersionView> componentVersionViews = componentRequestService.getAllItemsFromLink(componentView, UrlConstants.SEGMENT_VERSIONS, ComponentVersionView.class);

        return componentVersionViews;
    }

    public List<VersionBomComponentView> getAllComponentVersionsFromProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView projectVersionView = projectVersionRequestService.getProjectVersion(projectItem, projectVersionName);
        final List<VersionBomComponentView> versionBomComponentViews = projectVersionRequestService.getAllItemsFromLink(projectVersionView, MetaService.COMPONENTS_LINK, VersionBomComponentView.class);

        return versionBomComponentViews;
    }

}
