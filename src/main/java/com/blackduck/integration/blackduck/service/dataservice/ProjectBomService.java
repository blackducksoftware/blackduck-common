/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.*;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.blackduck.integration.blackduck.service.model.VersionBomComponentModel;
import com.blackduck.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.body.BodyContentConverter;
import com.blackduck.integration.rest.response.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectBomService extends DataService {
    private final ComponentService componentService;

    public ProjectBomService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, ComponentService componentService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.componentService = componentService;
    }

    public List<ProjectVersionComponentVersionView> getComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentVersionView> projectVersionComponentVersionViews = blackDuckApiClient.getAllResponses(projectVersionView.metaComponentsLink());
        return projectVersionComponentVersionViews;
    }

    public List<ProjectVersionVulnerableBomComponentsView> getVulnerableComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionVulnerableBomComponentsView> vulnerableBomComponentViews = blackDuckApiClient.getAllResponses(projectVersionView.metaVulnerableComponentsLink());
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentVersionView> ProjectVersionComponentViews = getComponentsForProjectVersion(projectVersionView);
        List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (ProjectVersionComponentVersionView projectVersionComponentVersionView : ProjectVersionComponentViews) {
            if (StringUtils.isNotBlank(projectVersionComponentVersionView.getComponentVersion())) {
                HttpUrl projectVersionComponentUrl = new HttpUrl(projectVersionComponentVersionView.getComponentVersion());
                ComponentVersionView componentVersionView = blackDuckApiClient.getResponse(projectVersionComponentUrl, ComponentVersionView.class);
                componentVersionViews.add(componentVersionView);
            }
        }

        List<ComponentVersionVulnerabilities> componentVersionVulnerabilitiesList = new ArrayList<>();
        for (ComponentVersionView componentVersionView : componentVersionViews) {
            ComponentVersionVulnerabilities componentVersionVulnerabilities = componentService.getComponentVersionVulnerabilities(componentVersionView);
            componentVersionVulnerabilitiesList.add(componentVersionVulnerabilities);
        }
        return componentVersionVulnerabilitiesList;
    }

    public List<VersionBomComponentModel> getComponentsWithMatchedFilesForProjectVersion(ProjectVersionView version) throws IntegrationException {
        List<ProjectVersionComponentVersionView> bomComponents = blackDuckApiClient.getAllResponses(version.metaComponentsLink());
        List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (ProjectVersionComponentVersionView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    public Optional<ProjectVersionPolicyStatusView> getPolicyStatusForVersion(ProjectVersionView version) throws IntegrationException {
        if (version.metaPolicyStatusLinkSafely().isPresent()) {
            return Optional.ofNullable(blackDuckApiClient.getResponse(version.metaPolicyStatusLink()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<ProjectVersionPolicyRulesView>> getActivePoliciesForVersion(ProjectVersionView version) {
        UrlMultipleResponses<ProjectVersionPolicyRulesView> url = version.metaActivePolicyRulesLink();
        try {
            return Optional.ofNullable(blackDuckApiClient.getAllResponses(url));
        } catch (IntegrationException e) {
            return Optional.empty();
        }
    }

    //TODO investigate what variant is
    public Optional<String> addComponentToProjectVersion(ExternalId componentExternalId, ProjectVersionView projectVersionView) throws IntegrationException {
        HttpUrl projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);
        Optional<ComponentsView> componentSearchResultView = componentService.getFirstOrEmptyResult(componentExternalId);
        String componentVersionUrl = null;
        if (componentSearchResultView.isPresent()) {
            if (StringUtils.isNotBlank(componentSearchResultView.get().getVariant())) {
                componentVersionUrl = componentSearchResultView.get().getVariant();
            } else if (StringUtils.isNotBlank(componentSearchResultView.get().getVersion())) {
                componentVersionUrl = componentSearchResultView.get().getVersion();
            } else {
                componentVersionUrl = componentSearchResultView.get().getComponent();
            }
            addComponentToProjectVersion(new HttpUrl(componentVersionUrl), projectVersionComponentsUrl);
        }

        return Optional.ofNullable(componentVersionUrl);
    }

    public void addProjectVersionToProjectVersion(ProjectVersionView projectVersionViewToAdd, ProjectVersionView targetProjectVersionView) throws IntegrationException {
        HttpUrl toAddUrl = projectVersionViewToAdd.getHref();
        HttpUrl targetUrl = targetProjectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);

        addComponentToProjectVersion(toAddUrl, targetUrl);
    }

    public void addComponentToProjectVersion(ComponentVersionView componentVersionView, ProjectVersionView projectVersionView) throws IntegrationException {
        HttpUrl componentVersionUrl = componentVersionView.getHref();
        HttpUrl projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK);

        addComponentToProjectVersion(componentVersionUrl, projectVersionComponentsUrl);
    }

    public void addComponentToProjectVersion(HttpUrl componentVersionUrl, HttpUrl projectVersionComponentsUrl) throws IntegrationException {
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
            .postString("{\"component\": \"" + componentVersionUrl.string() + "\"}", BodyContentConverter.DEFAULT)
            .buildBlackDuckResponseRequest(projectVersionComponentsUrl);
        try (Response response = blackDuckApiClient.execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private List<ComponentMatchedFilesView> getMatchedFiles(ProjectVersionComponentVersionView component) throws IntegrationException {
        List<ComponentMatchedFilesView> matchedFiles = new ArrayList<>(0);
        List<ComponentMatchedFilesView> tempMatchedFiles = blackDuckApiClient.getAllResponses(component.metaMatchedFilesLink());
        if (tempMatchedFiles != null && !tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
