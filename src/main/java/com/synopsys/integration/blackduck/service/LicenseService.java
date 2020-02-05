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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.Optional;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseLicensesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.VersionBomLicenseView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Response;

public class LicenseService extends DataService {
    private final ComponentService componentDataService;

    public LicenseService(final BlackDuckService blackDuckService, final IntLogger logger, final ComponentService componentDataService) {
        super(blackDuckService, logger);
        this.componentDataService = componentDataService;
    }

    public Optional<ProjectVersionLicenseView> getComplexLicenseItemFromComponent(final ExternalId externalId) throws IntegrationException {
        final Optional<ComponentsView> componentSearchView = componentDataService.getFirstOrEmptyResult(externalId);
        if (!componentSearchView.isPresent()) {
            return Optional.empty();
        }

        final String componentVersionUrl = componentSearchView.get().getVersion();
        final ComponentVersionView componentVersion = blackDuckService.getResponse(componentVersionUrl, ComponentVersionView.class);

        return Optional.ofNullable(componentVersion.getLicense());
    }

    public LicenseView getLicenseView(final VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        return getLicenseView(versionBomLicenseView.getLicense());
    }

    public LicenseView getLicenseView(final ProjectVersionLicenseLicensesView ProjectVersionLicenseLicensesView) throws IntegrationException {
        return getLicenseView(ProjectVersionLicenseLicensesView.getLicense());
    }

    public LicenseView getLicenseView(final String licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        final LicenseView licenseView = blackDuckService.getResponse(licenseUrl, LicenseView.class);
        return licenseView;
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        final String licenseTextUrl = licenseView.getFirstLink(LicenseView.TEXT_LINK).orElse(null);
        try (Response response = blackDuckService.get(licenseTextUrl)) {
            return response.getContentString();
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
}
