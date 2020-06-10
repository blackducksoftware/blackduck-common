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

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.*;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.blackduck.service.model.VersionBomComponentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectBomService extends DataService {
    private final ComponentService componentService;

    public ProjectBomService(BlackDuckService blackDuckService, IntLogger logger, ComponentService componentService) {
        super(blackDuckService, logger);
        this.componentService = componentService;
    }

    public List<ProjectVersionComponentView> getComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentView> ProjectVersionComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return ProjectVersionComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VulnerableComponentView> vulnerableBomComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ProjectVersionView projectVersionView) throws IntegrationException {
        List<ProjectVersionComponentView> ProjectVersionComponentViews = getComponentsForProjectVersion(projectVersionView);
        List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (ProjectVersionComponentView projectVersionComponentView : ProjectVersionComponentViews) {
            if (StringUtils.isNotBlank(projectVersionComponentView.getComponentVersion())) {
                HttpUrl projectVersionComponentUrl = new HttpUrl(projectVersionComponentView.getComponentVersion());
                ComponentVersionView componentVersionView = blackDuckService.getResponse(projectVersionComponentUrl, ComponentVersionView.class);
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
        List<ProjectVersionComponentView> bomComponents = blackDuckService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (ProjectVersionComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    public Optional<ProjectVersionPolicyStatusView> getPolicyStatusForVersion(ProjectVersionView version) throws IntegrationException {
        return blackDuckService.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    //TODO investigate what variant is
    public Optional<String> addComponentToProjectVersion(ExternalId componentExternalId, ProjectVersionView projectVersionView) throws IntegrationException {
        String projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK)
                .orElseThrow(() -> new IntegrationException(String.format("The ProjectVersionView does not have a components link.\n%s", projectVersionView)));
        Optional<ComponentsView> componentSearchResultView = componentService.getFirstOrEmptyResult(componentExternalId);
        String componentVersionUrl = null;
        if (componentSearchResultView.isPresent()) {
            if (StringUtils.isNotBlank(componentSearchResultView.get().getVariant())) {
                componentVersionUrl = componentSearchResultView.get().getVariant();
            } else {
                componentVersionUrl = componentSearchResultView.get().getVersion();
            }
            addComponentToProjectVersion(new HttpUrl(projectVersionComponentsUrl), new HttpUrl(componentVersionUrl));
        }

        return Optional.ofNullable(componentVersionUrl);
    }

    public void addProjectVersionToProjectVersion(ProjectVersionView projectVersionViewToAdd, ProjectVersionView targetProjectVersionView) throws IntegrationException {
        String toAdd = projectVersionViewToAdd.getHref().orElseThrow(() -> new IntegrationException(String.format("The ProjectVersionView to add does not have an href.\n%s", projectVersionViewToAdd)));
        String target = targetProjectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK)
                .orElseThrow(() -> new IntegrationException(String.format("The target ProjectVersionView does not have a '%' link.\n%s", ProjectVersionView.COMPONENTS_LINK, targetProjectVersionView)));

        HttpUrl toAddUrl = new HttpUrl(toAdd);
        HttpUrl targetUrl = new HttpUrl(target);

        addComponentToProjectVersion(toAddUrl, targetUrl);
    }

    public void addComponentToProjectVersion(ComponentVersionView componentVersionView, ProjectVersionView projectVersionView) throws IntegrationException {
        String componentVersionLink = componentVersionView.getHref().orElseThrow(() -> new IntegrationException(String.format("The ComponentVersionView does not have an href.\n%s", componentVersionView)));
        String projectVersionComponentsLink = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK)
                .orElseThrow(() -> new IntegrationException(String.format("The ProjectVersionView does not have a components link.\n%s", projectVersionView)));

        HttpUrl componentVersionUrl = new HttpUrl(componentVersionLink);
        HttpUrl projectVersionComponentsUrl = new HttpUrl(projectVersionComponentsLink);

        addComponentToProjectVersion(componentVersionUrl, projectVersionComponentsUrl);
    }

    public void addComponentToProjectVersion(HttpUrl componentVersionUrl, HttpUrl projectVersionComponentsUrl) throws IntegrationException {
        Request request = RequestFactory.createCommonPostRequestBuilder(projectVersionComponentsUrl, "{\"component\": \"" + componentVersionUrl.string() + "\"}").build();
        try (Response response = blackDuckService.execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private List<ComponentMatchedFilesView> getMatchedFiles(ProjectVersionComponentView component) throws IntegrationException {
        List<ComponentMatchedFilesView> matchedFiles = new ArrayList<>(0);
        List<ComponentMatchedFilesView> tempMatchedFiles = blackDuckService.getAllResponses(component, ProjectVersionComponentView.MATCHED_FILES_LINK_RESPONSE);
        if (tempMatchedFiles != null && !tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
