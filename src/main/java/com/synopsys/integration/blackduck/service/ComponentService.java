/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityV2View;
import com.synopsys.integration.blackduck.service.model.BlackDuckMediaTypes;
import com.synopsys.integration.blackduck.service.model.BlackDuckQuery;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class ComponentService extends DataService {
    public static final String REMEDIATING_LINK = "remediating";
    public static final LinkSingleResponse<RemediationOptionsView> REMEDIATION_OPTIONS_LINK_RESPONSE = new LinkSingleResponse<>(REMEDIATING_LINK, RemediationOptionsView.class);

    public ComponentService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public Optional<ComponentVersionView> getComponentVersion(final ExternalId externalId) throws IntegrationException {
        for (final ComponentVersionView componentVersion : getAllComponentVersions(externalId)) {
            if (componentVersion.getVersionName().equals(externalId.version)) {
                return Optional.of(componentVersion);
            }
        }
        final String errMsg = "Could not find version " + externalId.version + " of component " + externalId.createBlackDuckOriginId();
        logger.error(errMsg);
        return Optional.empty();
    }

    public List<ComponentVersionView> getAllComponentVersions(final ExternalId externalId) throws IntegrationException {
        final Optional<ComponentSearchResultView> componentSearchView = getExactComponentMatch(externalId);
        if (!componentSearchView.isPresent()) {
            return Collections.emptyList();
        }
        final ComponentView componentView = blackDuckService.getResponse(componentSearchView.get().getComponent(), ComponentView.class);

        final List<ComponentVersionView> componentVersionViews = blackDuckService.getAllResponses(componentView, ComponentView.VERSIONS_LINK_RESPONSE);
        return componentVersionViews;
    }

    public Optional<ComponentSearchResultView> getExactComponentMatch(final ExternalId externalId) throws IntegrationException {
        final List<ComponentSearchResultView> allComponents = getAllComponents(externalId);
        final String hubOriginIdToMatch = externalId.createBlackDuckOriginId();
        for (final ComponentSearchResultView componentItem : allComponents) {
            if (null != hubOriginIdToMatch) {
                if (hubOriginIdToMatch.equals(componentItem.getOriginId())) {
                    return Optional.of(componentItem);
                }
            }
        }
        logger.error("Couldn't find an exact component that matches " + hubOriginIdToMatch);
        return Optional.empty();
    }

    public List<ComponentSearchResultView> getAllComponents(final ExternalId externalId) throws IntegrationException {
        final String forge = externalId.forge.getName();
        final String hubOriginId = externalId.createBlackDuckOriginId();
        final String componentQuery = String.format("%s|%s", forge, hubOriginId);
        final Optional<BlackDuckQuery> hubQuery = BlackDuckQuery.createQuery("id", componentQuery);

        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);
        final List<ComponentSearchResultView> allComponents = blackDuckService.getAllResponses(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestBuilder);
        return allComponents;
    }

    public List<VulnerabilityV2View> getVulnerabilitiesFromComponentVersion(final ExternalId externalId) throws IntegrationException {
        final Optional<ComponentVersionVulnerabilities> componentVersionVulnerabilities = getComponentVersionVulnerabilities(externalId);
        if (!componentVersionVulnerabilities.isPresent()) {
            return Collections.emptyList();
        }
        return componentVersionVulnerabilities.get().getVulnerabilities();
    }

    public Optional<ComponentVersionVulnerabilities> getComponentVersionVulnerabilities(final ExternalId externalId) throws IntegrationException {
        final Optional<ComponentSearchResultView> componentSearchView = getExactComponentMatch(externalId);
        if (!componentSearchView.isPresent()) {
            return Optional.empty();
        }

        final String componentVersionURL = componentSearchView.get().getVersion();
        if (null != componentVersionURL) {
            final ComponentVersionView componentVersion = blackDuckService.getResponse(componentVersionURL, ComponentVersionView.class);
            return Optional.ofNullable(getComponentVersionVulnerabilities(componentVersion));
        }

        logger.error("Couldn't get a componentVersion url from the component matching " + externalId.createExternalId());
        return Optional.empty();
    }

    public ComponentVersionVulnerabilities getComponentVersionVulnerabilities(final ComponentVersionView componentVersion) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().mimeType(BlackDuckMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        final List<VulnerabilityV2View> vulnerabilityList = blackDuckService.getAllResponses(componentVersion, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE, requestBuilder);
        return new ComponentVersionVulnerabilities(componentVersion, vulnerabilityList);
    }

    // TODO deprecate when the REMEDIATING_LINK is included in ComponentVersionView
    public Optional<RemediationOptionsView> getRemediationInformation(final ComponentVersionView componentVersionView) throws IntegrationException {
        if (!componentVersionView.getHref().isPresent()) {
            return Optional.empty();
        }

        final String remediatingUrl = componentVersionView.getHref().get() + "/" + REMEDIATING_LINK;
        final UriSingleResponse<RemediationOptionsView> uriSingleResponse = new UriSingleResponse<>(remediatingUrl, RemediationOptionsView.class);
        return Optional.ofNullable(blackDuckService.getResponse(uriSingleResponse));
    }

}
