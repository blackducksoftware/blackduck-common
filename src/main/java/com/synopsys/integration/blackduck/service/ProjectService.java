/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectService extends DataService {
    private final ProjectGetService projectGetService;

    public ProjectService(BlackDuckService blackDuckService, IntLogger logger, ProjectGetService projectGetService) {
        super(blackDuckService, logger);
        this.projectGetService = projectGetService;
    }

    public ProjectVersionWrapper createProject(ProjectRequest projectRequest) throws IntegrationException {
        String projectUrl = blackDuckService.post(ApiDiscovery.PROJECTS_LINK, projectRequest);
        ProjectView projectView = blackDuckService.getResponse(projectUrl, ProjectView.class);
        if (null == projectRequest.getVersionRequest()) {
            return new ProjectVersionWrapper(projectView);
        }

        Optional<ProjectVersionView> projectVersionView = getProjectVersion(projectView, projectRequest.getVersionRequest().getVersionName());
        return new ProjectVersionWrapper(projectView, projectVersionView.orElse(null));
    }

    public ProjectVersionView createProjectVersion(ProjectView projectView, ProjectVersionRequest projectVersionRequest) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.VERSIONS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a version.", ProjectView.VERSIONS_LINK));
        }
        String projectVersionUrl = blackDuckService.post(projectView.getFirstLink(ProjectView.VERSIONS_LINK).get(), projectVersionRequest);
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
            // nothing exists, so create and return
            ProjectRequest projectRequest = projectSyncModel.createProjectRequest();
            return createProject(projectRequest);
        }

        // the project exists, so do updating and then deal with the version
        ProjectView projectView = optionalProjectView.get();
        if (performUpdate) {
            projectSyncModel.populateProjectView(projectView);
            blackDuckService.put(projectView);
            projectView = blackDuckService.getResponse(projectView.getHref().get(), ProjectView.class);
        }
        ProjectVersionView projectVersionView = null;

        // dealing with the version
        if (projectSyncModel.shouldHandleProjectVersion()) {
            String projectVersionName = projectSyncModel.getVersionName();
            Optional<ProjectVersionView> optionalProjectVersionView = getProjectVersion(projectView, projectVersionName);
            if (optionalProjectVersionView.isPresent()) {
                // the version already exists, so do updating
                projectVersionView = optionalProjectVersionView.get();
                if (performUpdate) {
                    projectSyncModel.populateProjectVersionView(projectVersionView);
                    blackDuckService.put(projectVersionView);
                    projectVersionView = blackDuckService.getResponse(projectVersionView.getHref().get(), ProjectVersionView.class);
                }
            } else {
                // the version did not exist, so create it
                ProjectVersionRequest projectVersionRequest = projectSyncModel.createProjectVersionRequest();
                projectVersionView = createProjectVersion(projectView, projectVersionRequest);
            }
        }

        return new ProjectVersionWrapper(projectView, projectVersionView);
    }

}
