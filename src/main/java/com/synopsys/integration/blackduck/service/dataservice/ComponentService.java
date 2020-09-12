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
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionRemediatingView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.http.BlackDuckMediaTypes;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class ComponentService extends DataService {
    public static final String REMEDIATING_LINK = "remediating";
    public static final LinkSingleResponse<RemediationOptionsView> REMEDIATION_OPTIONS_LINK_RESPONSE = new LinkSingleResponse<>(ComponentService.REMEDIATING_LINK, RemediationOptionsView.class);

    public static final Function<List<ComponentsView>, Optional<ComponentsView>> FIRST_OR_EMPTY_RESULT = (list) -> Optional.ofNullable(list)
                                                                                                                       .filter(notEmptyList -> notEmptyList.size() > 0)
                                                                                                                       .map(notEmptyList -> notEmptyList.get(0));

    public static final Function<List<ComponentsView>, Optional<ComponentsView>> SINGLE_OR_EMPTY_RESULT = (list) -> Optional.ofNullable(list)
                                                                                                                        .filter(notEmptyList -> notEmptyList.size() == 1)
                                                                                                                        .map(listOfSingleElement -> listOfSingleElement.get(0));

    public ComponentService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger) {
        super(blackDuckService, requestFactory, logger);
    }

    public List<ComponentsView> getAllSearchResults(ExternalId externalId) throws IntegrationException {
        String forge = externalId.getForge().getName();
        String originId = externalId.createExternalId();
        String componentQuery = String.format("%s|%s", forge, originId);
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("id", componentQuery);

        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(blackDuckQuery);
        List<ComponentsView> allSearchResults = blackDuckService.getAllResponses(ApiDiscovery.COMPONENTS_LINK_RESPONSE, requestBuilder);
        return allSearchResults;
    }

    public Optional<ComponentsView> getSingleOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), SINGLE_OR_EMPTY_RESULT);
    }

    public Optional<ComponentsView> getFirstOrEmptyResult(ExternalId externalId) throws IntegrationException {
        return getFilteredSearchResults(getAllSearchResults(externalId), FIRST_OR_EMPTY_RESULT);
    }

    public <T> T getFilteredSearchResults(List<ComponentsView> searchResults, Function<List<ComponentsView>, T> filterFunction) {
        return filterFunction.apply(searchResults);
    }

    public <T> T getFilteredSearchResults(ExternalId externalId, List<ComponentsView> searchResults, BiFunction<ExternalId, List<ComponentsView>, T> filterFunction) {
        return filterFunction.apply(externalId, searchResults);
    }

    public Optional<ComponentVersionView> getComponentVersionView(ComponentsView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            HttpUrl url = new HttpUrl(searchResult.getVersion());
            return Optional.ofNullable(blackDuckService.getResponse(url, ComponentVersionView.class));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ComponentView> getComponentView(ComponentsView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getVersion())) {
            HttpUrl url = new HttpUrl(searchResult.getVersion());
            return Optional.ofNullable(blackDuckService.getResponse(url, ComponentView.class));
        } else {
            return Optional.empty();
        }
    }

    public ComponentVersionVulnerabilities getComponentVersionVulnerabilities(ComponentVersionView componentVersion) throws IntegrationException {
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder()
                                                     .acceptMimeType(BlackDuckMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        List<VulnerabilityView> vulnerabilityList = blackDuckService.getAllResponses(componentVersion, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE, requestBuilder);
        return new ComponentVersionVulnerabilities(componentVersion, vulnerabilityList);
    }

    // TODO deprecate when the REMEDIATING_LINK is included in ComponentVersionView
    public Optional<ComponentVersionRemediatingView> getRemediationInformation(ComponentVersionView componentVersionView) throws IntegrationException {
        String remediatingUrl = componentVersionView.getHref() + "/" + ComponentService.REMEDIATING_LINK;
        LinkSingleResponse<ComponentVersionRemediatingView> linkSingleResponse = new LinkSingleResponse<>(remediatingUrl, ComponentVersionRemediatingView.class);
        return Optional.ofNullable(blackDuckService.getResponse(linkSingleResponse));
    }

}
