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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentSearchResultView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.service.model.BlackDuckMediaTypes;
import com.synopsys.integration.blackduck.service.model.BlackDuckQuery;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class ComponentService extends DataService {
    public static final String REMEDIATING_LINK = "remediating";
    public static final LinkSingleResponse<RemediationOptionsView> REMEDIATION_OPTIONS_LINK_RESPONSE = new LinkSingleResponse<>(ComponentService.REMEDIATING_LINK, RemediationOptionsView.class);

    public ComponentService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public Optional<ComponentVersionView> getComponentVersion(ExternalId externalId) throws IntegrationException {
        for (ComponentVersionView componentVersion : getAllComponentVersions(externalId)) {
            if (componentVersion.getVersionName().equals(externalId.version)) {
                return Optional.of(componentVersion);
            }
        }
        String errMsg = "Could not find version " + externalId.version + " of component " + externalId.createBlackDuckOriginId();
        logger.error(errMsg);
        return Optional.empty();
    }

    public List<ComponentVersionView> getAllComponentVersions(ExternalId externalId) throws IntegrationException {
        Optional<ComponentSearchResultView> componentSearchView = getExactComponentMatch(externalId);
        if (!componentSearchView.isPresent()) {
            return Collections.emptyList();
        }
        ComponentView componentView = blackDuckService.getResponse(componentSearchView.get().getComponent(), ComponentView.class);

        List<ComponentVersionView> componentVersionViews = blackDuckService.getAllResponses(componentView, ComponentView.VERSIONS_LINK_RESPONSE);
        return componentVersionViews;
    }

    public Optional<ComponentSearchResultView> getExactComponentMatch(ExternalId externalId) throws IntegrationException {
        List<ComponentSearchResultView> allComponents = getAllComponents(externalId);
        String originIdToMatch = externalId.createBlackDuckOriginId();
        for (ComponentSearchResultView componentItem : allComponents) {
            if (null != originIdToMatch) {
                if (originIdToMatch.equals(componentItem.getOriginId())) {
                    return Optional.of(componentItem);
                }
            }
        }
        logger.error("Couldn't find an exact component that matches " + originIdToMatch);
        return Optional.empty();
    }

    public List<ComponentSearchResultView> getAllComponents(ExternalId externalId) throws IntegrationException {
        String forge = externalId.forge.getName();
        String originId = externalId.createBlackDuckOriginId();
        String componentQuery = String.format("%s|%s", forge, originId);
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("id", componentQuery);

        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        List<ComponentSearchResultView> allComponents = blackDuckService.getAllResponses(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestBuilder);
        return allComponents;
    }

    public List<VulnerabilityView> getVulnerabilitiesFromComponentVersion(ExternalId externalId) throws IntegrationException {
        Optional<ComponentVersionVulnerabilities> componentVersionVulnerabilities = getComponentVersionVulnerabilities(externalId);
        if (!componentVersionVulnerabilities.isPresent()) {
            return Collections.emptyList();
        }
        return componentVersionVulnerabilities.get().getVulnerabilities();
    }

    public Optional<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(ExternalId externalId) throws IntegrationException {
        Optional<ComponentSearchResultView> componentSearchView = getExactComponentMatch(externalId);
        if (!componentSearchView.isPresent()) {
            return Optional.empty();
        }

        String componentVersionURL = componentSearchView.get().getVersion();
        if (null != componentVersionURL) {
            ComponentVersionView componentVersion = blackDuckService.getResponse(componentVersionURL, ComponentVersionView.class);
            return Optional.ofNullable(getComponentVersionVulnerabilities(componentVersion));
        }

        logger.error("Couldn't get a componentVersion url from the component matching " + externalId.createExternalId());
        return Optional.empty();
    }

    public ComponentVersionVulnerabilities getComponentVersionVulnerabilities(ComponentVersionView componentVersion) throws IntegrationException {
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().mimeType(BlackDuckMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        List<VulnerabilityView> vulnerabilityList = blackDuckService.getAllResponses(componentVersion, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE, requestBuilder);
        return new ComponentVersionVulnerabilities(componentVersion, vulnerabilityList);
    }

    // TODO deprecate when the REMEDIATING_LINK is included in ComponentVersionView
    public Optional<RemediationOptionsView> getRemediationInformation(ComponentVersionView componentVersionView) throws IntegrationException {
        if (!componentVersionView.getHref().isPresent()) {
            return Optional.empty();
        }

        String remediatingUrl = componentVersionView.getHref().get() + "/" + ComponentService.REMEDIATING_LINK;
        UriSingleResponse<RemediationOptionsView> uriSingleResponse = new UriSingleResponse<>(remediatingUrl, RemediationOptionsView.class);
        return Optional.ofNullable(blackDuckService.getResponse(uriSingleResponse));
    }

}
