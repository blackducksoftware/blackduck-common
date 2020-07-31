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

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.response.ComponentsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseLicensesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;

import java.io.IOException;
import java.util.Optional;

public class LicenseService extends DataService {
    private final ComponentService componentDataService;

    public LicenseService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger, ComponentService componentDataService) {
        super(blackDuckService, requestFactory, logger);
        this.componentDataService = componentDataService;
    }

    public Optional<ProjectVersionLicenseView> getComplexLicenseItemFromComponent(ExternalId externalId) throws IntegrationException {
        Optional<ComponentsView> componentSearchView = componentDataService.getFirstOrEmptyResult(externalId);
        if (!componentSearchView.isPresent()) {
            return Optional.empty();
        }

        HttpUrl componentVersionUrl = new HttpUrl(componentSearchView.get().getVersion());
        ComponentVersionView componentVersion = blackDuckService.getResponse(componentVersionUrl, ComponentVersionView.class);

        return Optional.ofNullable(componentVersion.getLicense());
    }

    public LicenseView getLicenseView(VersionBomLicenseView versionBomLicenseView) throws IntegrationException {
        HttpUrl url = new HttpUrl(versionBomLicenseView.getLicense());
        return getLicenseView(url);
    }

    public LicenseView getLicenseView(ProjectVersionLicenseLicensesView projectVersionLicenseLicensesView) throws IntegrationException {
        HttpUrl url = new HttpUrl(projectVersionLicenseLicensesView.getLicense());
        return getLicenseView(url);
    }

    public LicenseView getLicenseView(HttpUrl licenseUrl) throws IntegrationException {
        if (licenseUrl == null) {
            return null;
        }
        LicenseView licenseView = blackDuckService.getResponse(licenseUrl, LicenseView.class);
        return licenseView;
    }

    public String getLicenseText(LicenseView licenseView) throws IntegrationException {
        HttpUrl licenseTextUrl = licenseView.getFirstLink(LicenseView.TEXT_LINK);
        try (Response response = blackDuckService.get(licenseTextUrl)) {
            return response.getContentString();
        } catch (IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }
}
