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
package com.blackducksoftware.integration.hub.dataservice.component;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomService;
import com.blackducksoftware.integration.hub.api.component.ComponentService;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.MatchedFileView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.matchedfiles.MatchedFilesService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.dataservice.component.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService {
    private final IntLogger logger;
    private final ProjectService projectRequestService;
    private final ProjectVersionService projectVersionRequestService;
    private final ComponentService componentRequestService;
    private final AggregateBomService aggregateBomService;
    private final MatchedFilesService matchedFilesService;
    private final MetaHandler metaHandler;

    public ComponentDataService(final IntLogger logger, final ProjectService projectRequestService, final ProjectVersionService projectVersionRequestService, final ComponentService componentRequestService,
            final AggregateBomService aggregateBomService, final MatchedFilesService matchedFilesService) {
        this.logger = logger;
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.componentRequestService = componentRequestService;
        this.aggregateBomService = aggregateBomService;
        this.matchedFilesService = matchedFilesService;
        this.metaHandler = new MetaHandler(logger);
    }

    public ComponentVersionView getExactComponentVersionFromComponent(final ExternalId externalId) throws IntegrationException {
        for (final ComponentVersionView componentVersion : this.getAllComponentVersionsFromComponent(externalId)) {
            if (componentVersion.versionName.equals(externalId.version)) {
                return componentVersion;
            }
        }
        final String errMsg = "Could not find version " + externalId.version + " of component " + externalId.createHubOriginId();
        logger.error(errMsg);
        throw new HubIntegrationException(errMsg);
    }

    public List<ComponentVersionView> getAllComponentVersionsFromComponent(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = componentRequestService.getExactComponentMatch(externalId);

        final ComponentView componentView = componentRequestService.getResponse(componentSearchView.component, ComponentView.class);
        final List<ComponentVersionView> componentVersionViews = componentRequestService.getAllResponsesFromLink(componentView, UrlConstants.SEGMENT_VERSIONS, ComponentVersionView.class);

        return componentVersionViews;
    }

    public void addComponentToProjectVersion(final ExternalId componentExternalId, final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView projectVersionView = projectVersionRequestService.getProjectVersion(projectView, projectVersionName);
        final String projectVersionComponentsUrl = projectVersionRequestService.getFirstLink(projectVersionView, MetaHandler.COMPONENTS_LINK);

        final ComponentSearchResultView componentSearchResultView = componentRequestService.getExactComponentMatch(componentExternalId);
        final String componentVersionUrl = componentSearchResultView.version;

        aggregateBomService.addBomComponent("application/json", projectVersionComponentsUrl, componentVersionUrl);
    }

    public List<VersionBomComponentView> getAllComponentVersionsFromProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectItem = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView projectVersionView = projectVersionRequestService.getProjectVersion(projectItem, projectVersionName);
        final List<VersionBomComponentView> versionBomComponentViews = projectVersionRequestService.getAllResponsesFromLink(projectVersionView, MetaHandler.COMPONENTS_LINK, VersionBomComponentView.class);

        return versionBomComponentViews;
    }

    public List<VersionBomComponentModel> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getComponentsForProjectVersion(version);
    }

    public List<VersionBomComponentModel> getComponentsForProjectVersion(final ProjectVersionView version) throws IntegrationException {
        final List<VersionBomComponentView> bomComponents = aggregateBomService.getBomEntries(version);
        final List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (final VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    private List<MatchedFileView> getMatchedFiles(final VersionBomComponentView component) {
        try {
            final String matchedFilesLink = metaHandler.getFirstLink(component, MetaHandler.MATCHED_FILES_LINK);
            return matchedFilesService.getMatchedFiles(matchedFilesLink);
        } catch (final IntegrationException e) {
            return new ArrayList<>(0);
        }
    }

}
