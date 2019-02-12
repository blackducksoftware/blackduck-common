package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.view.ComponentSearchResultView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.MatchedFileView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.blackduck.service.model.VersionBomComponentModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class ProjectBomService extends DataService {
    private final ComponentService componentService;

    public ProjectBomService(BlackDuckService blackDuckService, IntLogger logger, ComponentService componentService) {
        super(blackDuckService, logger);
        this.componentService = componentService;
    }

    public List<VersionBomComponentView> getComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VersionBomComponentView> versionBomComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        return versionBomComponentViews;
    }

    public List<VulnerableComponentView> getVulnerableComponentsForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VulnerableComponentView> vulnerableBomComponentViews = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
        return vulnerableBomComponentViews;
    }

    public List<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ProjectVersionView projectVersionView) throws IntegrationException {
        List<VersionBomComponentView> versionBomComponentViews = getComponentsForProjectVersion(projectVersionView);
        List<ComponentVersionView> componentVersionViews = new ArrayList<>();
        for (VersionBomComponentView versionBomComponentView : versionBomComponentViews) {
            if (StringUtils.isNotBlank(versionBomComponentView.getComponentVersion())) {
                ComponentVersionView componentVersionView = blackDuckService.getResponse(versionBomComponentView.getComponentVersion(), ComponentVersionView.class);
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
        List<VersionBomComponentView> bomComponents = blackDuckService.getAllResponses(version, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        List<VersionBomComponentModel> modelBomComponents = new ArrayList<>(bomComponents.size());
        for (VersionBomComponentView component : bomComponents) {
            modelBomComponents.add(new VersionBomComponentModel(component, getMatchedFiles(component)));
        }
        return modelBomComponents;
    }

    public Optional<VersionBomPolicyStatusView> getPolicyStatusForVersion(ProjectVersionView version) throws IntegrationException {
        return blackDuckService.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
    }

    public Optional<String> addComponentToProjectVersion(ExternalId componentExternalId, ProjectVersionView projectVersionView) throws IntegrationException {
        String projectVersionComponentsUrl = projectVersionView.getFirstLink(ProjectVersionView.COMPONENTS_LINK).orElse(null);
        Optional<ComponentSearchResultView> componentSearchResultView = componentService.getExactComponentMatch(componentExternalId);
        String componentVersionUrl = null;
        if (componentSearchResultView.isPresent()) {
            if (StringUtils.isNotBlank(componentSearchResultView.get().getVariant())) {
                componentVersionUrl = componentSearchResultView.get().getVariant();
            } else {
                componentVersionUrl = componentSearchResultView.get().getVersion();
            }
            addComponentToProjectVersion("application/json", projectVersionComponentsUrl, componentVersionUrl);
        }

        return Optional.ofNullable(componentVersionUrl);
    }

    public void addComponentToProjectVersion(String mediaType, String projectVersionComponentsUri, String componentVersionUrl) throws IntegrationException {
        Request request = RequestFactory.createCommonPostRequestBuilder("{\"component\": \"" + componentVersionUrl + "\"}").uri(projectVersionComponentsUri).mimeType(mediaType).build();
        try (Response response = blackDuckService.execute(request)) {
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    private List<MatchedFileView> getMatchedFiles(VersionBomComponentView component) throws IntegrationException {
        List<MatchedFileView> matchedFiles = new ArrayList<>(0);
        List<MatchedFileView> tempMatchedFiles = blackDuckService.getAllResponses(component, VersionBomComponentView.MATCHED_FILES_LINK_RESPONSE);
        if (tempMatchedFiles != null && !tempMatchedFiles.isEmpty()) {
            matchedFiles = tempMatchedFiles;
        }
        return matchedFiles;
    }

}
