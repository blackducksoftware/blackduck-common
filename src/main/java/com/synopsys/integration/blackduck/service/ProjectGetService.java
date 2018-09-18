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
package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.model.HubQuery;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class ProjectGetService extends DataService {
    public ProjectGetService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
    }

    public List<ProjectView> getAllProjectMatches(final String projectName) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);

        final List<ProjectView> allProjectItems = hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder);
        return allProjectItems;
    }

    public List<ProjectView> getProjectMatches(final String projectName, final int limit) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("name", projectName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery, limit, RequestFactory.DEFAULT_OFFSET);

        final List<ProjectView> projectItems = hubService.getResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE, requestBuilder, false);
        return projectItems;
    }

    public ProjectView getProjectByName(final String projectName) throws IntegrationException {
        final List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (final ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.name)) {
                return project;
            }
        }
        throw new DoesNotExistException("This Project does not exist. Project: " + projectName);
    }

    public ProjectVersionView getProjectVersion(final ProjectView project, final String projectVersionName) throws IntegrationException {
        final Optional<HubQuery> hubQuery = HubQuery.createQuery("versionName", projectVersionName);
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);

        final List<ProjectVersionView> allProjectVersionMatchingItems = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE, requestBuilder);
        final ProjectVersionView projectVersion = findMatchingVersion(allProjectVersionMatchingItems, projectVersionName);
        if (null != projectVersion) {
            return projectVersion;
        }
        throw new DoesNotExistException(String.format("Could not find the version: %s for project: %s", projectVersionName, project.name));
    }

    public ProjectVersionWrapper getProjectVersion(final String projectName, final String projectVersionName) throws IntegrationException {
        final ProjectView projectView = getProjectByName(projectName);
        final ProjectVersionView projectVersionView = getProjectVersion(projectView, projectVersionName);

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

    public ProjectVersionView findMatchingVersion(final List<ProjectVersionView> projectVersions, final String projectVersionName) throws HubIntegrationException {
        for (final ProjectVersionView version : projectVersions) {
            if (projectVersionName.equals(version.versionName)) {
                return version;
            }
        }
        return null;
    }

}
