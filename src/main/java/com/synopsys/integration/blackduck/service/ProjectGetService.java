/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

import java.util.List;
import java.util.Optional;

public class ProjectGetService extends DataService {
    public ProjectGetService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectView> allProjectItems = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectView> projectItems = blackDuckService.getSomeResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder, limit);
        return projectItems;
    }

    public Optional<ProjectView> getProjectViewByProjectName(String projectName) throws IntegrationException {
        List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.getName())) {
                return Optional.of(project);
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getProjectVersionViewByProjectVersionName(ProjectView projectView, String projectVersionName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("versionName", projectVersionName);
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectVersionView> allProjectVersionMatchingItems = blackDuckService.getAllResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder);
        Optional<ProjectVersionView> projectVersion = findMatchingProjectVersionView(allProjectVersionMatchingItems, projectVersionName);

        return projectVersion;
    }

    public Optional<ProjectVersionView> findMatchingProjectVersionView(List<ProjectVersionView> projectVersions, String projectVersionName) {
        for (ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.getVersionName())) {
                return Optional.of(version);
            }
        }

        return Optional.empty();
    }

}
