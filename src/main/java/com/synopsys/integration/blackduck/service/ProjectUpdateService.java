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

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class ProjectUpdateService extends DataService {
    private final ProjectGetService projectGetService;

    public ProjectUpdateService(final HubService hubService, final IntLogger logger, final ProjectGetService projectGetService) {
        super(hubService, logger);
        this.projectGetService = projectGetService;
    }

    public String createProject(final ProjectRequest projectRequest) throws IntegrationException {
        final String json = hubService.convertToJson(projectRequest);
        final Request.Builder requestBuilder = RequestFactory.createCommonPostRequestBuilder(json);
        return hubService.executePostRequestAndRetrieveURL(ApiDiscovery.PROJECTS_LINK, requestBuilder);
    }

    public void updateProject(final String projectUrl, final ProjectRequest projectRequest) throws IntegrationException {
        final String json = hubService.convertToJson(projectRequest);
        final Request projectUpdateRequest = RequestFactory.createCommonPutRequestBuilder(json).uri(projectUrl).build();
        try (Response response = hubService.executeRequest(projectUpdateRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteProject(final String projectUrl) throws IntegrationException {
        final Request deleteRequest = new Request.Builder(projectUrl).method(HttpMethod.DELETE).build();
        try (Response response = hubService.executeRequest(deleteRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public String createProjectVersion(final String projectVersionsUrl, final ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        final String json = hubService.convertToJson(projectVersionRequest);
        final Request request = RequestFactory.createCommonPostRequestBuilder(json).uri(projectVersionsUrl).build();
        return hubService.executePostRequestAndRetrieveURL(request);
    }

    public void updateProjectVersion(final String projectVersionUrl, final ProjectVersionRequest versionRequest) throws IntegrationException {
        final String json = hubService.convertToJson(versionRequest);
        final Request projectVersionUpdateRequest = RequestFactory.createCommonPutRequestBuilder(json).uri(projectVersionUrl).build();
        try (Response response = hubService.executeRequest(projectVersionUpdateRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteProjectVersion(final String projectVersionUrl) throws IntegrationException {
        final Request deleteRequest = new Request.Builder(projectVersionUrl).method(HttpMethod.DELETE).build();
        try (Response response = hubService.executeRequest(deleteRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    /**
     * If the project exists, the project will be updated.
     * If the project does not exist it will be created.
     *
     * If the version is provided and the version exists, it will be updated.
     * If the version is provided and the version does not exist, it will be created.
     *
     * This method should not be used to update the names of the project or version as these are used to find the necessary
     * urls to update with.
     */
    public ProjectVersionWrapper syncProjectAndVersion(final ProjectRequest projectRequest) throws IntegrationException {
        return syncProjectAndVersion(projectRequest, true);
    }

    /**
     * If the project exists and performUpdate is true, the project will be updated.
     * If the project does not exist it will be created.
     *
     * If the version is provided and the version exists and performUpdate is true, it will be updated.
     * If the version is provided and the version does not exist, it will be created.
     *
     * This method should not be used to update the names of the project or version as these are used to find the necessary
     * urls to update with.
     */
    public ProjectVersionWrapper syncProjectAndVersion(final ProjectRequest projectRequest, final boolean performUpdate) throws IntegrationException {
        ProjectView projectView = null;
        ProjectVersionView projectVersionView = null;

        final Optional<ProjectView> optionalProject = projectGetService.getProjectViewByProjectName(projectRequest.getName());
        if (optionalProject.isPresent()) {
            projectView = optionalProject.get();
            if (performUpdate) {
                final String projectUrl = projectView.getHref().orElse(null);
                updateProject(projectUrl, projectRequest);
            }
        } else {
            final String projectUrl = createProject(projectRequest);
            projectView = hubService.getResponse(projectUrl, ProjectView.class);
        }

        final ProjectVersionRequest projectVersionRequest = projectRequest.getVersionRequest();
        if (projectVersionRequest != null && StringUtils.isNotBlank(projectVersionRequest.getVersionName())) {
            final Optional<ProjectVersionView> optionalVersion = projectGetService.getProjectVersionViewByProjectVersionName(projectView, projectVersionRequest.getVersionName());
            if (optionalVersion.isPresent()) {
                projectVersionView = optionalVersion.get();
                if (performUpdate) {
                    final String projectVersionUrl = projectVersionView.getHref().orElse(null);
                    updateProjectVersion(projectVersionUrl, projectRequest.getVersionRequest());
                }
            } else {
                final String projectVersionsUrl = projectView.getFirstLink(ProjectView.VERSIONS_LINK).orElse(null);
                final String projectVersionUrl = createProjectVersion(projectVersionsUrl, projectRequest.getVersionRequest());
                projectVersionView = hubService.getResponse(projectVersionUrl, ProjectVersionView.class);
            }
        }

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

    public void updateProjectAndVersion(final String projectUri, final String projectVersionUri, final ProjectRequest projectRequest) throws IntegrationException {
        final String json = hubService.convertToJson(projectRequest);
        final Request projectUpdateRequest = RequestFactory.createCommonPutRequestBuilder(json).uri(projectUri).build();
        try (Response response = hubService.executeRequest(projectUpdateRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
        updateProjectVersion(projectVersionUri, projectRequest.getVersionRequest());
    }

}
