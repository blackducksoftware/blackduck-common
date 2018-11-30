/**
 * blackduck-common
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
package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.view.ScanSummaryView;
import com.synopsys.integration.blackduck.service.model.BlackDuckQuery;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.request.Request;

public class CodeLocationService extends DataService {
    public CodeLocationService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public void unmapCodeLocations(final List<CodeLocationView> codeLocationViews) throws IntegrationException {
        for (final CodeLocationView codeLocationView : codeLocationViews) {
            unmapCodeLocation(codeLocationView);
        }
    }

    public void unmapCodeLocation(final CodeLocationView codeLocationView) throws IntegrationException {
        mapCodeLocation(codeLocationView, "");
    }

    public void mapCodeLocation(final CodeLocationView codeLocationView, final ProjectVersionView version) throws IntegrationException {
        if (version.getHref().isPresent()) {
            mapCodeLocation(codeLocationView, version.getHref().get());
        }
    }

    public void mapCodeLocation(final CodeLocationView codeLocationView, final String versionUrl) throws IntegrationException {
        codeLocationView.setMappedProjectVersion(versionUrl);
        blackDuckService.put(codeLocationView);
    }

    public Optional<CodeLocationView> getCodeLocationByName(final String codeLocationName) throws IntegrationException {
        if (StringUtils.isNotBlank(codeLocationName)) {
            final Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", codeLocationName);
            final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(blackDuckQuery);
            final List<CodeLocationView> codeLocations = blackDuckService.getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, requestBuilder);
            for (final CodeLocationView codeLocation : codeLocations) {
                if (codeLocationName.equals(codeLocation.getName())) {
                    return Optional.of(codeLocation);
                }
            }
            return codeLocations
                           .stream()
                           .filter(codeLocationView -> codeLocationName.equals(codeLocationView.getName()))
                           .findFirst();
        }

        logger.error(String.format("The code location (%s) does not exist.", codeLocationName));
        return Optional.empty();
    }

    public CodeLocationView getCodeLocationById(final String codeLocationId) throws IntegrationException {
        final BlackDuckPath blackDuckPath = new BlackDuckPath(ApiDiscovery.CODELOCATIONS_LINK.getPath() + "/" + codeLocationId);
        final BlackDuckPathSingleResponse<CodeLocationView> codeLocationResponse = new BlackDuckPathSingleResponse<>(blackDuckPath, CodeLocationView.class);
        return blackDuckService.getResponse(codeLocationResponse);
    }

    public ScanSummaryView getScanSummaryViewById(final String scanSummaryId) throws IntegrationException {
        final String uri = BlackDuckService.SCANSUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        return blackDuckService.getResponse(uri, ScanSummaryView.class);
    }

}