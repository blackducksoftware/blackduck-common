/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.blackduck.integration.blackduck.http.BlackDuckQuery;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectGetService extends DataService {
    private final UrlMultipleResponses<ProjectView> projectsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.PROJECTS_PATH);

    public ProjectGetService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("name", projectName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);
        BlackDuckMultipleRequest<ProjectView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectsResponses);

        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("name", projectName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);
        BlackDuckMultipleRequest<ProjectView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectsResponses);

        return blackDuckApiClient.getSomeResponses(requestMultiple, limit);
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
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("versionName", projectVersionName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);

        BlackDuckMultipleRequest<ProjectVersionView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectView.metaVersionsLink());
        Predicate<ProjectVersionView> predicate = projectVersionView -> projectVersionName.equals(projectVersionView.getVersionName());

        return blackDuckApiClient.getSomeMatchingResponses(requestMultiple, predicate, 1)
                   .stream()
                   .findFirst();
    }

}
