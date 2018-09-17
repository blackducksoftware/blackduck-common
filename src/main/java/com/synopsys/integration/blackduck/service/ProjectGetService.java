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
