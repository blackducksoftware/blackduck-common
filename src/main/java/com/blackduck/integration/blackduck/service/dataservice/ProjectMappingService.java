/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.manual.view.ProjectMappingView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;

import java.util.List;

public class ProjectMappingService extends DataService {
    public ProjectMappingService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public void populateApplicationId(ProjectView projectView, String applicationId) throws IntegrationException {
        List<ProjectMappingView> projectMappings = blackDuckApiClient.getAllResponses(projectView.metaProjectMappingsLink());
        boolean canCreate = projectMappings.isEmpty();
        if (canCreate) {
            if (!projectView.hasLink(ProjectView.PROJECT_MAPPINGS_LINK)) {
                throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a project mapping.", ProjectView.PROJECT_MAPPINGS_LINK));
            }
            HttpUrl projectMappingsLink = projectView.getFirstLink(ProjectView.PROJECT_MAPPINGS_LINK);
            ProjectMappingView projectMappingView = new ProjectMappingView();
            projectMappingView.setApplicationId(applicationId);
            blackDuckApiClient.post(projectMappingsLink, projectMappingView);
        } else {
            // Currently there exists only one project-mapping which is the project's Application ID.
            // Eventually, this method would need to take in a namespace on which we will need to filter.
            ProjectMappingView projectMappingView = projectMappings.get(0);
            projectMappingView.setApplicationId(applicationId);
            blackDuckApiClient.put(projectMappingView);
        }
    }

    public List<ProjectMappingView> getProjectMappings(ProjectView projectView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectView.metaProjectMappingsLink());
    }

}
