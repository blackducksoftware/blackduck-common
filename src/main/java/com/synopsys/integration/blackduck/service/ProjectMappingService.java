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

import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.ProjectMappingView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectMappingService extends DataService {
    public ProjectMappingService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    /**
     * Sets the applicationId for a project
     * @throws IntegrationException
     */
    public void populateApplicationId(ProjectView projectView, String applicationId) throws IntegrationException {
        List<ProjectMappingView> projectMappings = blackDuckService.getAllResponses(projectView, ProjectView.PROJECT_MAPPINGS_LINK_RESPONSE);
        boolean canCreate = projectMappings.isEmpty();
        if (canCreate) {
            if (!projectView.hasLink(ProjectView.PROJECT_MAPPINGS_LINK)) {
                throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a project mapping.", ProjectView.PROJECT_MAPPINGS_LINK));
            }
            String projectMappingsLink = projectView.getFirstLink(ProjectView.PROJECT_MAPPINGS_LINK).get();
            ProjectMappingView projectMappingView = new ProjectMappingView();
            projectMappingView.setApplicationId(applicationId);
            blackDuckService.post(projectMappingsLink, projectMappingView);
        } else {
            // Currently there exists only one project-mapping which is the project's Application ID.
            // Eventually, this method would need to take in a namespace on which we will need to filter.
            ProjectMappingView projectMappingView = projectMappings.get(0);
            projectMappingView.setApplicationId(applicationId);
            blackDuckService.put(projectMappingView);
        }
    }

    public List<ProjectMappingView> getProjectMappings(ProjectView projectView) throws IntegrationException {
        return blackDuckService.getResponses(projectView, ProjectView.PROJECT_MAPPINGS_LINK_RESPONSE, true);
    }

}
