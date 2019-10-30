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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComponentService extends DataService {
    public static final String REMEDIATING_LINK = "remediating";
    public static final LinkSingleResponse<RemediationOptionsView> REMEDIATION_OPTIONS_LINK_RESPONSE = new LinkSingleResponse<>(ComponentService.REMEDIATING_LINK, RemediationOptionsView.class);

    public static final Function<List<ComponentSearchResultView>, Optional<ComponentSearchResultView>> FIRST_OR_EMPTY_RESULT = (list) -> Optional.ofNullable(list)
            .filter(notEmptyList -> notEmptyList.size() > 0)
            .map(notEmptyList -> notEmptyList.get(0));

    public static final Function<List<ComponentSearchResultView>, Optional<ComponentSearchResultView>> SINGLE_OR_EMPTY_RESULT = (list) -> Optional.ofNullable(list)
            .filter(notEmptyList -> notEmptyList.size() == 1)
            .map(listOfSingleElement -> listOfSingleElement.get(0));

    public ComponentService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<ComponentSearchResultView> getAllSearchResults(ExternalId externalId) throws IntegrationException {
        String forge = externalId.forge.getName();
        String originId = externalId.createExternalId();
        String componentQuery = String.format("%s|%s", forge, originId);
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("id", componentQuery);

        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        List<ComponentSearchResultView> allSearchResults = blackDuckService.getAllResponses(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestBuilder);
        return allSearchResults;
    }

    public Optional<ComponentSearchResultView> getSingleOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), SINGLE_OR_EMPTY_RESULT);
    }

    public Optional<ComponentSearchResultView> getFirstOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), FIRST_OR_EMPTY_RESULT);
    }

    public <T> T getFilteredSearchResults(List<ComponentSearchResultView> searchResults, Function<List<ComponentSearchResultView>, T> filterFunction) {
        return filterFunction.apply(searchResults);
    }

    public <T> T getFilteredSearchResults(ExternalId externalId, List<ComponentSearchResultView> searchResults, BiFunction<ExternalId, List<ComponentSearchResultView>, T> filterFunction) {
        return filterFunction.apply(externalId, searchResults);
    }

    public Optional<ComponentVersionView> getComponentVersionView(ComponentSearchResultView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            return Optional.ofNullable(blackDuckService.getResponse(searchResult.getVersion(), ComponentVersionView.class));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ComponentView> getComponentView(ComponentSearchResultView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            return Optional.ofNullable(blackDuckService.getResponse(searchResult.getComponent(), ComponentView.class));
        } else {
            return Optional.empty();
        }
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
