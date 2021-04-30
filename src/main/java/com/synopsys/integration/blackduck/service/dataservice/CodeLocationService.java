/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ScanSummaryView;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class CodeLocationService extends DataService {
    // as of at least 2019.6.0, code location names in Black Duck are case-insensitive
    public static final BiPredicate<String, CodeLocationView> NAME_MATCHER = (codeLocationName, codeLocationView) -> codeLocationName.equalsIgnoreCase(codeLocationView.getName());

    public CodeLocationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, blackDuckRequestBuilderFactory, logger);
    }

    public List<CodeLocationView> getAllCodeLocations() throws IntegrationException {
        UrlMultipleResponses<CodeLocationView> codeLocationsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.CODELOCATIONS_PATH);
        return blackDuckApiClient.getAllResponses(codeLocationsResponses);
    }

    public void unmapCodeLocations(List<CodeLocationView> codeLocationViews) throws IntegrationException {
        for (CodeLocationView codeLocationView : codeLocationViews) {
            unmapCodeLocation(codeLocationView);
        }
    }

    public void unmapCodeLocation(HttpUrl codeLocationUrl) throws IntegrationException {
        CodeLocationView codeLocationView = createFakeCodeLocationView(codeLocationUrl);
        mapCodeLocation(codeLocationView, (HttpUrl) null);
    }

    public void unmapCodeLocation(CodeLocationView codeLocationView) throws IntegrationException {
        mapCodeLocation(codeLocationView, (HttpUrl) null);
    }

    public void mapCodeLocation(HttpUrl codeLocationUrl, ProjectVersionView projectVersionView) throws IntegrationException {
        CodeLocationView codeLocationView = createFakeCodeLocationView(codeLocationUrl);
        mapCodeLocation(codeLocationView, projectVersionView);
    }

    public void mapCodeLocation(CodeLocationView codeLocationView, ProjectVersionView version) throws IntegrationException {
        mapCodeLocation(codeLocationView, version.getHref());
    }

    public void mapCodeLocation(CodeLocationView codeLocationView, HttpUrl versionUrl) throws IntegrationException {
        codeLocationView.setMappedProjectVersion(null == versionUrl ? "" : versionUrl.string());
        blackDuckApiClient.put(codeLocationView);
    }

    public Optional<CodeLocationView> getCodeLocationByName(String codeLocationName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", codeLocationName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory
                                                              .createCommonGetRequestBuilder(blackDuckQuery)
            .setBlackDuckPageDefinition(new BlackDuckPageDefinition(1, 0));

        Predicate<CodeLocationView> predicate = codeLocationView -> NAME_MATCHER.test(codeLocationName, codeLocationView);

        UrlMultipleResponses<CodeLocationView> codeLocationResponse = apiDiscovery.metaMultipleResponses(ApiDiscovery.CODELOCATIONS_PATH);
        return blackDuckApiClient.getSomeMatchingResponses(codeLocationResponse, blackDuckRequestBuilder, predicate, 1)
                   .stream()
                   .findFirst();
    }

    public CodeLocationView getCodeLocationById(String codeLocationId) throws IntegrationException {
        BlackDuckPath blackDuckPath = new BlackDuckPath(ApiDiscovery.CODELOCATIONS_PATH.getPath() + "/" + codeLocationId, CodeLocationView.class, false);
        UrlSingleResponse<CodeLocationView> codeLocationResponse = apiDiscovery.metaSingleResponse(blackDuckPath);
        return blackDuckApiClient.getResponse(codeLocationResponse);
    }

    public ScanSummaryView getScanSummaryViewById(String scanSummaryId) throws IntegrationException {
        String uri = BlackDuckApiClient.SCANSUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        HttpUrl url = new HttpUrl(uri);
        return blackDuckApiClient.getResponse(url, ScanSummaryView.class);
    }

    private CodeLocationView createFakeCodeLocationView(final HttpUrl codeLocationUrl) {
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(codeLocationUrl);
        CodeLocationView codeLocationView = new CodeLocationView();
        codeLocationView.setMeta(resourceMetadata);

        NullNode pathJsonNode = new JsonNodeFactory(false).nullNode();
        codeLocationView.setPatch(pathJsonNode);

        return codeLocationView;
    }

}
