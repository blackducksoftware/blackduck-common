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
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionLicenseLicensesView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionLicenseView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.LicenseView;
import com.blackduck.integration.blackduck.api.manual.temporary.component.VersionBomLicenseView;
import com.blackduck.integration.blackduck.http.BlackDuckQuery;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.blackduck.service.request.BlackDuckRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.response.Response;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LicenseService extends DataService {
    private final ComponentService componentDataService;

    public LicenseService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger, ComponentService componentDataService) {
        super(blackDuckApiClient, apiDiscovery, logger);
        this.componentDataService = componentDataService;
    }

    public Optional<ComponentVersionLicenseView> getComplexLicenseItemFromComponent(ExternalId externalId) throws IntegrationException {
        Optional<ComponentsView> componentSearchView = componentDataService.getFirstOrEmptyResult(externalId);
        if (!componentSearchView.isPresent()) {
            return Optional.empty();
        }

        HttpUrl componentVersionUrl = new HttpUrl(componentSearchView.get().getVersion());
        ComponentVersionView componentVersion = blackDuckApiClient.getResponse(componentVersionUrl, ComponentVersionView.class);

        return Optional.ofNullable(componentVersion.getLicense());
    }

    public LicenseView getLicenseView(VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        HttpUrl url = new HttpUrl(versionBomLicenseView.getLicense());
        return getLicenseView(url);
    }

    public LicenseView getLicenseView(ComponentVersionLicenseLicensesView componentVersionLicenseLicensesView) throws IntegrationException {
        HttpUrl url = new HttpUrl(componentVersionLicenseLicensesView.getLicense());
        return getLicenseView(url);
    }

    public LicenseView getLicenseView(HttpUrl licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        LicenseView licenseView = blackDuckApiClient.getResponse(licenseUrl, LicenseView.class);
        return licenseView;
    }

    public String getLicenseText(LicenseView licenseView) throws IntegrationException {
        HttpUrl licenseTextUrl = licenseView.getFirstLink(LicenseView.TEXT_LINK);
        try (Response response = blackDuckApiClient.get(licenseTextUrl)) {
            return response.getContentString();
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public List<LicenseView> searchLicensesByName(String licenseName) throws IntegrationException {
        BlackDuckQuery nameQuery = new BlackDuckQuery("name:" + licenseName);
        BlackDuckRequest<LicenseView, UrlMultipleResponses<LicenseView>> requestMultiple = new BlackDuckRequest<>(new BlackDuckRequestBuilder().commonGet().addBlackDuckQuery(nameQuery), apiDiscovery.metaLicensesLink());
        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public Optional<HttpUrl> getLicenseUrlByLicenseName(String licenseName) {
        try {
            return blackDuckApiClient.getAllResponses(apiDiscovery.metaLicensesLink()).stream()
                .filter(license -> license.getName().equals(licenseName))
                .findFirst()
                .map(LicenseView::getHref);
        } catch (IntegrationException e) {
            return Optional.empty();
        }
    }


}
