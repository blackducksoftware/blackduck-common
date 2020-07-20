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
package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProjectService extends DataService {
    private final ProjectGetService projectGetService;

    public ProjectService(BlackDuckService blackDuckService, IntLogger logger, ProjectGetService projectGetService) {
        super(blackDuckService, logger);
        this.projectGetService = projectGetService;
    }

    public List<ProjectView> getAllProjects() throws IntegrationException {
        return blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
    }

    public ProjectVersionWrapper createProject(ProjectRequest projectRequest) throws IntegrationException {
        HttpUrl projectUrl = blackDuckService.post(ApiDiscovery.PROJECTS_LINK, projectRequest);
        ProjectView projectView = blackDuckService.getResponse(projectUrl, ProjectView.class);
        if (null == projectRequest.getVersionRequest()) {
            return new ProjectVersionWrapper(projectView);
        }

        Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView, projectRequest.getVersionRequest().getVersionName());
        return new ProjectVersionWrapper(projectView, projectVersionView.orElse(null));
    }

    public List<ProjectVersionView> getAllProjectVersions(ProjectView projectView) throws IntegrationException {
        return blackDuckService.getAllResponses(projectView, ProjectView.VERSIONS_LINK_RESPONSE);
    }

    public ProjectVersionView createProjectVersion(ProjectView projectView, ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.VERSIONS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a version.", ProjectView.VERSIONS_LINK));
        }
        HttpUrl projectVersionUrl = blackDuckService.post(projectView.getFirstLink(ProjectView.VERSIONS_LINK).get(), projectVersionRequest);
        return blackDuckService.getResponse(projectVersionUrl, ProjectVersionView.class);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        return projectGetService.getAllProjectMatches(projectName);
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        return projectGetService.getProjectMatches(projectName, limit);
    }

    public Optional<ProjectView> getProjectByName(String projectName) throws IntegrationException {
        return projectGetService.getProjectViewByProjectName(projectName);
    }

    public Optional<ProjectVersionView> getProjectVersion(ProjectView project, String projectVersionName) throws IntegrationException {
        return projectGetService.getProjectVersionViewByProjectVersionName(project, projectVersionName);
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(NameVersion projectAndVersion) throws IntegrationException {
        return getProjectVersion(projectAndVersion.getName(), projectAndVersion.getVersion());
    }

    public Optional<ProjectVersionWrapper> getProjectVersion(String projectName, String projectVersionName) throws IntegrationException {
        Optional<ProjectView> projectView = getProjectByName(projectName);
        if (projectView.isPresent()) {
            Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView.get(), projectVersionName);

            if (projectVersionView.isPresent()) {
                return Optional.of(new ProjectVersionWrapper(projectView.get(), projectVersionView.get()));
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getNewestProjectVersion(ProjectView projectView) throws IntegrationException {
        List<ProjectVersionView> projectVersionViews = getAllProjectVersions(projectView);
        return projectVersionViews.stream().max(Comparator.comparing(ProjectVersionView::getCreatedAt));
    }

    public void updateProject(ProjectView projectView) throws IntegrationException {
        blackDuckService.put(projectView);
    }

    public void updateProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        blackDuckService.put(projectVersionView);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectSyncModel projectSyncModel) throws IntegrationException {
        return syncProjectAndVersion(projectSyncModel, false);
    }

    public ProjectVersionWrapper syncProjectAndVersion(ProjectSyncModel projectSyncModel, boolean performUpdate) throws IntegrationException {
        String projectName = projectSyncModel.getName();

        Optional<ProjectView> optionalProjectView = getProjectByName(projectName);
        if (!optionalProjectView.isPresent()) {
            logger.info(String.format("The %s project was not found, so it will be created - if a version was included, it will also be created.", projectName));
            ProjectRequest projectRequest = projectSyncModel.createProjectRequest();
            return createProject(projectRequest);
        }

        ProjectView projectView = optionalProjectView.get();
        if (performUpdate) {
            logger.info(String.format("The %s project was found and performUpdate=true, so it will be updated.", projectName));
            projectSyncModel.populateProjectView(projectView);
            blackDuckService.put(projectView);
            projectView = blackDuckService.getResponse(projectView.getHref().get(), ProjectView.class);
        }
        ProjectVersionView projectVersionView = null;

        if (projectSyncModel.shouldHandleProjectVersion()) {
            String projectVersionName = projectSyncModel.getVersionName();
            Optional<ProjectVersionView> optionalProjectVersionView = getProjectVersion(projectView, projectVersionName);
            if (optionalProjectVersionView.isPresent()) {
                projectVersionView = optionalProjectVersionView.get();
                if (performUpdate) {
                    logger.info(String.format("The %s version was found and performUpdate=true, so the version will be updated.", projectVersionName));
                    projectSyncModel.populateProjectVersionView(projectVersionView);
                    blackDuckService.put(projectVersionView);
                    projectVersionView = blackDuckService.getResponse(projectVersionView.getHref().get(), ProjectVersionView.class);
                }
            } else {
                logger.info(String.format("The %s version was not found, so it will be created under the %s project.", projectVersionName, projectName));
                ProjectVersionRequest projectVersionRequest = projectSyncModel.createProjectVersionRequest();
                projectVersionView = createProjectVersion(projectView, projectVersionRequest);
            }
        }

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

}
