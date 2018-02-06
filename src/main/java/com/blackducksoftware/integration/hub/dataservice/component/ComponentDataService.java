/**
 * hub-common
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
package com.blackducksoftware.integration.hub.dataservice.component;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_COMPONENTS;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UrlConstants;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV1View;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityService;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService extends HubService {
    private final IntLogger logger;
    private final VulnerabilityService vulnerabilityService;

    public ComponentDataService(final RestConnection restConnection, final VulnerabilityService vulnerabilityService) {
        super(restConnection);
        this.logger = restConnection.logger;
        this.vulnerabilityService = vulnerabilityService;
    }

    public ComponentVersionView getExactComponentVersionFromComponent(final ExternalId externalId) throws IntegrationException {
        for (final ComponentVersionView componentVersion : this.getAllComponentVersionsFromComponent(externalId)) {
            if (componentVersion.versionName.equals(externalId.version)) {
                return componentVersion;
            }
        }
        final String errMsg = "Could not find version " + externalId.version + " of component " + externalId.createHubOriginId();
        logger.error(errMsg);
        throw new HubIntegrationException(errMsg);
    }

    public List<ComponentVersionView> getAllComponentVersionsFromComponent(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = getExactComponentMatch(externalId);

        final ComponentView componentView = getResponse(componentSearchView.component, ComponentView.class);
        final List<ComponentVersionView> componentVersionViews = getAllResponsesFromLink(componentView, UrlConstants.SEGMENT_VERSIONS, ComponentVersionView.class);
        return componentVersionViews;
    }

    public ComponentSearchResultView getExactComponentMatch(final ExternalId externalId) throws IntegrationException {
        final List<ComponentSearchResultView> allComponents = getAllComponents(externalId);
        final String hubOriginIdToMatch = externalId.createHubOriginId();
        for (final ComponentSearchResultView componentItem : allComponents) {
            if (null != hubOriginIdToMatch) {
                if (hubOriginIdToMatch.equals(componentItem.originId)) {
                    return componentItem;
                }
            }
        }
        throw new HubIntegrationException("Couldn't find an exact component that matches " + hubOriginIdToMatch);
    }

    public List<ComponentSearchResultView> getAllComponents(final ExternalId externalId) throws IntegrationException {
        final String forge = externalId.forge.getName();
        final String hubOriginId = externalId.createHubOriginId();
        final String componentQuery = String.format("id:%s|%s", forge, hubOriginId);
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(Arrays.asList(SEGMENT_API, SEGMENT_COMPONENTS), componentQuery);

        final List<ComponentSearchResultView> allComponents = getAllResponses(hubPagedRequest, ComponentSearchResultView.class);
        return allComponents;
    }

    public List<VulnerabilityV1View> getVulnerabilitiesFromComponentVersion(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = getExactComponentMatch(externalId);
        final String componentVersionURL = componentSearchView.version;
        if (null != componentVersionURL) {
            final ComponentVersionView componentVersion = getResponse(componentVersionURL, ComponentVersionView.class);
            final String vulnerabilitiesLink = getFirstLink(componentVersion, MetaHandler.VULNERABILITIES_LINK);
            final List<VulnerabilityV1View> vulnerabilityList = vulnerabilityService.getComponentVersionVulnerabilities(vulnerabilitiesLink);
            return vulnerabilityList;
        }

        throw new HubIntegrationException("Couldn't get a componentVersion url from the component matching " + externalId.createExternalId());
    }

}
