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
import java.util.function.Function;
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ScanSummaryView;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class CodeLocationService extends DataService {
    // as of at least 2019.6.0, code location names in Black Duck are case-insensitive
    public static final Function<String, Predicate<CodeLocationView>> NAME_MATCHER = (codeLocationName) -> (codeLocationView) -> codeLocationName.equalsIgnoreCase(codeLocationView.getName());

    public CodeLocationService(BlackDuckService blackDuckService, RequestFactory requestFactory, IntLogger logger) {
        super(blackDuckService, requestFactory, logger);
    }

    public List<CodeLocationView> getAllCodeLocations() throws IntegrationException {
        return blackDuckService.getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE);
    }

    public void unmapCodeLocations(List<CodeLocationView> codeLocationViews) throws IntegrationException {
        for (CodeLocationView codeLocationView : codeLocationViews) {
            unmapCodeLocation(codeLocationView);
        }
    }

    public void unmapCodeLocation(CodeLocationView codeLocationView) throws IntegrationException {
        mapCodeLocation(codeLocationView, (HttpUrl) null);
    }

    public void mapCodeLocation(CodeLocationView codeLocationView, ProjectVersionView version) throws IntegrationException {
        mapCodeLocation(codeLocationView, version.getHref());
    }

    public void mapCodeLocation(CodeLocationView codeLocationView, HttpUrl versionUrl) throws IntegrationException {
        codeLocationView.setMappedProjectVersion(null == versionUrl ? "" : versionUrl.string());
        blackDuckService.put(codeLocationView);
    }

    public Predicate<CodeLocationView> createNameMatchingPredicate(String codeLocationName) {
        return (codeLocationView) -> codeLocationName.equalsIgnoreCase(codeLocationView.getName());
    }

    public Optional<CodeLocationView> getCodeLocationByName(String codeLocationName) throws IntegrationException {
        Optional<BlackDuckQuery> blackDuckQuery = BlackDuckQuery.createQuery("name", codeLocationName);
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(blackDuckQuery);

        Predicate<CodeLocationView> predicate = NAME_MATCHER.apply(codeLocationName);

        return blackDuckService.getSomeMatchingResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, requestBuilder, predicate, 1)
                   .stream()
                   .findFirst();
    }

    public CodeLocationView getCodeLocationById(String codeLocationId) throws IntegrationException {
        BlackDuckPath blackDuckPath = new BlackDuckPath(ApiDiscovery.CODELOCATIONS_LINK.getPath() + "/" + codeLocationId);
        BlackDuckPathSingleResponse<CodeLocationView> codeLocationResponse = new BlackDuckPathSingleResponse<>(blackDuckPath, CodeLocationView.class);
        return blackDuckService.getResponse(codeLocationResponse);
    }

    public ScanSummaryView getScanSummaryViewById(String scanSummaryId) throws IntegrationException {
        String uri = BlackDuckService.SCANSUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        HttpUrl url = new HttpUrl(uri);
        return blackDuckService.getResponse(url, ScanSummaryView.class);
    }

}
