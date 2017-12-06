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
package com.blackducksoftware.integration.hub.dataservice.versionbomcomponent;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.matchedfiles.MatchedFilesRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.dataservice.versionbomcomponent.model.VersionBomComponentModel;
import com.blackducksoftware.integration.hub.model.view.MatchedFilesView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.log.IntLogger;

public class VersionBomComponentDataService {
    private final ProjectRequestService projectRequestService;
    private final ProjectVersionRequestService projectVersionRequestService;
    private final AggregateBomRequestService aggregateBomRequestService;
    private final MatchedFilesRequestService matchedFilesRequestService;
    private final MetaService metaService;

    public VersionBomComponentDataService(final IntLogger logger, final ProjectRequestService projectRequestService, final ProjectVersionRequestService projectVersionRequestService,
            final AggregateBomRequestService aggregateBomRequestService, final MatchedFilesRequestService matchedFilesRequestService) {
        this.projectRequestService = projectRequestService;
        this.projectVersionRequestService = projectVersionRequestService;
        this.aggregateBomRequestService = aggregateBomRequestService;
        this.matchedFilesRequestService = matchedFilesRequestService;
        this.metaService = new MetaService(logger);
    }

    public List<VersionBomComponentModel> getComponentsForProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView project = projectRequestService.getProjectByName(projectName);
        final ProjectVersionView version = projectVersionRequestService.getProjectVersion(project, projectVersionName);
        return getComponentsForProjectVersion(version);
    }

    public List<VersionBomComponentModel> getComponentsForProjectVersion(final ProjectVersionView version) throws IntegrationException {
        final List<VersionBomComponentView> bomComponents = aggregateBomRequestService.getBomEntries(version);
        final List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (final VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    private List<MatchedFilesView> getMatchedFiles(final VersionBomComponentView component) {
        try {
            final String matchedFilesLink = metaService.getFirstLink(component, MetaService.MATCHED_FILES_LINK);
            return matchedFilesRequestService.getMatchedFiles(matchedFilesLink);
        } catch (final IntegrationException e) {
            return new ArrayList<>(0);
        }
    }
}
