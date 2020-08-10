/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.dataservice;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ProjectGetService extends DataService {
    public ProjectGetService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger) {
        super(blackDuckService, requestFactory, logger);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        List<ProjectView> allProjectItems = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", projectName);
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(blackDuckQuery);

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
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        Predicate<ProjectVersionView> predicate = projectVersionView -> projectVersionName.equals(projectVersionView.getVersionName());

        return blackDuckService.getSomeMatchingResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder, predicate, 1)
                   .stream()
                   .findFirst();
    }

}
