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

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.HubMediaTypes;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV1View;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.GetRequestWrapper;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public class ComponentDataService extends HubService {
    private final IntLogger logger;

    public ComponentDataService(final RestConnection restConnection) {
        super(restConnection);
        this.logger = restConnection.logger;
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
        final List<ComponentVersionView> componentVersionViews = getAllResponsesFromLink(componentView, "versions", ComponentVersionView.class);
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

        final GetRequestWrapper requestWrapper = new GetRequestWrapper();
        requestWrapper.setQ(componentQuery);
        final List<ComponentSearchResultView> allComponents = getAllResponsesFromApi(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestWrapper);
        return allComponents;
    }

    public List<VulnerabilityV1View> getVulnerabilitiesFromComponentVersion(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = getExactComponentMatch(externalId);
        final String componentVersionURL = componentSearchView.version;
        if (null != componentVersionURL) {
            final ComponentVersionView componentVersion = getResponse(componentVersionURL, ComponentVersionView.class);
            final String vulnerabilitiesLink = getFirstLink(componentVersion, MetaHandler.VULNERABILITIES_LINK);
            final GetRequestWrapper requestWrapper = new GetRequestWrapper();
            requestWrapper.setMimeType(HubMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
            final List<VulnerabilityV1View> vulnerabilityList = getAllResponses(vulnerabilitiesLink, VulnerabilityV1View.class, requestWrapper);
            return vulnerabilityList;
        }

        throw new HubIntegrationException("Couldn't get a componentVersion url from the component matching " + externalId.createExternalId());
    }

}
