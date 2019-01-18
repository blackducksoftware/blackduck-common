package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.view.ProjectMappingView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.exception.IntegrationException;

public class ProjectMappingService {
    private final BlackDuckService blackDuckService;

    public ProjectMappingService(final BlackDuckService blackDuckService) {
        this.blackDuckService = blackDuckService;
    }

    /**
     * Sets the applicationId for a project
     * @throws IntegrationException
     */
    // TODO: Currently there exists only one project-mapping which is the project's Application ID. Eventually there will be namespaces that we will need to filter. This method would need to take in a namespace as well.
    public void setApplicationId(final ProjectView projectView, final String applicationId) throws IntegrationException {
        final Optional<String> projectMappingsLink = projectView.getFirstLink(ProjectView.PROJECT_MAPPINGS_LINK);

        if (projectMappingsLink.isPresent()) {
            final Optional<ProjectMappingView> existingProjectMappingView = getProjectMappings(projectView).stream()
                                                                                .findFirst();
            if (existingProjectMappingView.isPresent()) {
                final ProjectMappingView projectMappingView = existingProjectMappingView.get();
                projectMappingView.setApplicationId(applicationId);
                blackDuckService.put(projectMappingView);
            } else {
                final ProjectMappingView ProjectMappingView = new ProjectMappingView();
                ProjectMappingView.setApplicationId(applicationId);
                blackDuckService.post(projectMappingsLink.get(), ProjectMappingView);
            }
        } else {
            throw new IntegrationException("project-mappings link not found in projectView");
        }
    }

    public List<ProjectMappingView> getProjectMappings(final ProjectView projectView) throws IntegrationException {
        return blackDuckService.getResponses(projectView, ProjectView.PROJECT_MAPPINGS_LINK_RESPONSE, true);
    }
}
