/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.core.BlackDuckPath;
import com.blackduck.integration.blackduck.api.core.ResourceMetadata;
import com.blackduck.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.CodeLocationView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.manual.view.ScanSummaryView;
import com.blackduck.integration.blackduck.http.BlackDuckQuery;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class CodeLocationService extends DataService {
    // as of at least 2019.6.0, code location names in Black Duck are case-insensitive
    public static final BiPredicate<String, CodeLocationView> NAME_MATCHER = (codeLocationName, codeLocationView) -> codeLocationName.equalsIgnoreCase(codeLocationView.getName());

    private final UrlMultipleResponses<CodeLocationView> codeLocationsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.CODELOCATIONS_PATH);

    public CodeLocationService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<CodeLocationView> getAllCodeLocations() throws IntegrationException {
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
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("name", codeLocationName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);

        Predicate<CodeLocationView> predicate = codeLocationView -> NAME_MATCHER.test(codeLocationName, codeLocationView);

        BlackDuckMultipleRequest<CodeLocationView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(codeLocationsResponses);
        return blackDuckApiClient.getSomeMatchingResponses(requestMultiple, predicate, 1)
                   .stream()
                   .findFirst();
    }

    public CodeLocationView getCodeLocationById(String codeLocationId) throws IntegrationException {
        BlackDuckPath blackDuckPath = new BlackDuckPath(ApiDiscovery.CODELOCATIONS_PATH.getPath() + "/" + codeLocationId, CodeLocationView.class, false);
        UrlSingleResponse<CodeLocationView> codeLocationResponse = apiDiscovery.metaSingleResponse(blackDuckPath);
        return blackDuckApiClient.getResponse(codeLocationResponse);
    }

    public ScanSummaryView getScanSummaryViewById(String scanSummaryId) throws IntegrationException {
        String uri = ApiDiscovery.SCAN_SUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        HttpUrl url = new HttpUrl(uri);
        return blackDuckApiClient.getResponse(url, ScanSummaryView.class);
    }

    private CodeLocationView createFakeCodeLocationView(HttpUrl codeLocationUrl) {
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(codeLocationUrl);
        CodeLocationView codeLocationView = new CodeLocationView();
        codeLocationView.setMeta(resourceMetadata);

        NullNode pathJsonNode = new JsonNodeFactory(false).nullNode();
        codeLocationView.setPatch(pathJsonNode);

        return codeLocationView;
    }

}
