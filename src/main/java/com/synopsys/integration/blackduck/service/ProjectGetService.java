/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.service.model.BlackDuckQuery;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class ProjectGetService extends DataService {
    public ProjectGetService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        final Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        final List<ProjectView> allProjectItems = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        final Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery, limit, RequestFactory.DEFAULT_OFFSET);

        final List<ProjectView> projectItems = blackDuckService.getResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder, false);
        return projectItems;
    }

    public Optional<ProjectView> getProjectViewByProjectName(final String projectName) throws IntegrationException {
        final List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.getName())) {
                return Optional.of(project);
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getProjectVersionViewByProjectVersionName(final ProjectView projectView, final String projectVersionName) throws IntegrationException {
        final Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("versionName", projectVersionName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        final List<ProjectVersionView> allProjectVersionMatchingItems = blackDuckService.getAllResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder);
        final Optional<ProjectVersionView> projectVersion = findMatchingProjectVersionView(allProjectVersionMatchingItems, projectVersionName);

        return projectVersion;
    }

    public Optional<ProjectVersionView> findMatchingProjectVersionView(final List<ProjectVersionView> projectVersions, final String projectVersionName) {
        for (final ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.getVersionName())) {
                return Optional.of(version);
            }
        }

        return Optional.empty();
    }

}
