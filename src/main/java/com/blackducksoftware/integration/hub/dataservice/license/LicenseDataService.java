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
package com.blackducksoftware.integration.hub.dataservice.license;

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.model.VersionBomLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.LicenseView;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class LicenseDataService extends HubService {
    private final ComponentDataService componentDataService;

    public LicenseDataService(final RestConnection restConnection, final ComponentDataService componentDataService) {
        super(restConnection);
        this.componentDataService = componentDataService;
    }

    public ComplexLicenseView getComplexLicenseItemFromComponent(final ExternalId externalId) throws IntegrationException {
        final ComponentSearchResultView componentSearchView = componentDataService.getExactComponentMatch(externalId);
        final String componentVersionUrl = componentSearchView.version;
        final ComponentVersionView componentVersion = getResponse(componentVersionUrl, ComponentVersionView.class);

        return componentVersion.license;
    }

    public LicenseView getLicenseView(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        return getLicenseView(versionBomLicenseView.license);
    }

    public LicenseView getLicenseView(final ComplexLicenseView complexLicenseView) throws IntegrationException {
        return getLicenseView(complexLicenseView.license);
    }

    public LicenseView getLicenseView(final String licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        final LicenseView licenseView = getResponse(licenseUrl, LicenseView.class);
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        final String licenseTextUrl = getFirstLinkSafely(licenseView, LicenseView.TEXT_LINK);
        final Request request = new Request(licenseTextUrl);
        try (Response response = getRestConnection().executeRequest(request)) {
            return response.getContentString();
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
}
