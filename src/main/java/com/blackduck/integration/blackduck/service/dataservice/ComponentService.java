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
import com.blackduck.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentView;
import com.blackduck.integration.blackduck.api.generated.view.VulnerabilityView;
import com.blackduck.integration.blackduck.http.BlackDuckMediaTypes;
import com.blackduck.integration.blackduck.http.BlackDuckQuery;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.model.ComponentVersionVulnerabilities;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComponentService extends DataService {
    public static final Function<List<ComponentsView>, Optional<ComponentsView>> FIRST_OR_EMPTY_RESULT =
        (list) ->
            Optional.ofNullable(list)
                .filter(notEmptyList -> notEmptyList.size() > 0)
                .map(notEmptyList -> notEmptyList.get(0));

    public static final Function<List<ComponentsView>, Optional<ComponentsView>> SINGLE_OR_EMPTY_RESULT =
        (list) ->
            Optional.ofNullable(list)
                .filter(notEmptyList -> notEmptyList.size() == 1)
                .map(listOfSingleElement -> listOfSingleElement.get(0));

    private final UrlMultipleResponses<ComponentsView> componentsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.COMPONENTS_PATH);

    public ComponentService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<ComponentsView> getAllSearchResults(ExternalId externalId) throws IntegrationException {
        String forge = externalId.getForge().getName();
        String originId = externalId.createExternalId();
        String componentQuery = String.format("%s|%s", forge, originId);
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("id", componentQuery);

        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);
        BlackDuckMultipleRequest<ComponentsView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(componentsResponses);

        return blackDuckApiClient.getAllResponses(requestMultiple);
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
            return Optional.ofNullable(blackDuckApiClient.getResponse(url, ComponentVersionView.class));
        } else {
            return Optional.empty();
        }
    }

    public Optional<ComponentView> getComponentView(ComponentsView searchResult) throws IntegrationException {
        if (StringUtils.isNotBlank(searchResult.getComponent())) {
            HttpUrl url = new HttpUrl(searchResult.getComponent());
            return Optional.ofNullable(blackDuckApiClient.getResponse(url, ComponentView.class));
        } else {
            return Optional.empty();
        }
    }

    public ComponentVersionVulnerabilities getComponentVersionVulnerabilities(ComponentVersionView componentVersion) throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .acceptMimeType(BlackDuckMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);

        BlackDuckMultipleRequest<VulnerabilityView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(componentVersion.metaVulnerabilitiesLink());
        List<VulnerabilityView> vulnerabilityList = blackDuckApiClient.getAllResponses(requestMultiple);
        return new ComponentVersionVulnerabilities(componentVersion, vulnerabilityList);
    }

    public Optional<ComponentVersionUpgradeGuidanceView> getUpgradeGuidance(ComponentVersionView componentVersionView) throws IntegrationException {
        if (componentVersionView.metaUpgradeGuidanceLinkSafely().isPresent()) {
            return Optional.ofNullable(blackDuckApiClient.getResponse(componentVersionView.metaUpgradeGuidanceLink()));
        } else {
            return Optional.empty();
        }
    }

}
