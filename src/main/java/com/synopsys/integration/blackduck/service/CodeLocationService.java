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
package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.core.HubPath;
import com.synopsys.integration.blackduck.api.core.HubPathSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.view.ScanSummaryView;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.service.model.HubQuery;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class CodeLocationService extends DataService {
    public CodeLocationService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
    }

    public void unmapCodeLocations(final List<CodeLocationView> codeLocationViews) throws IntegrationException {
        for (final CodeLocationView codeLocationView : codeLocationViews) {
            unmapCodeLocation(codeLocationView);
        }
    }

    public void unmapCodeLocation(final CodeLocationView codeLocationView) throws IntegrationException {
        final String codeLocationViewUrl = hubService.getHref(codeLocationView);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationView, "");
        updateCodeLocation(codeLocationViewUrl, hubService.getGson().toJson(requestCodeLocationView));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationView, final ProjectVersionView version) throws IntegrationException {
        mapCodeLocation(codeLocationView, hubService.getHref(version));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationView, final String versionUrl) throws IntegrationException {
        final String codeLocationViewUrl = hubService.getHref(codeLocationView);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationView, versionUrl);
        updateCodeLocation(codeLocationViewUrl, hubService.getGson().toJson(requestCodeLocationView));
    }

    public void updateCodeLocation(final CodeLocationView codeLocationView) throws IntegrationException {
        final String codeLocationViewUrl = hubService.getHref(codeLocationView);
        updateCodeLocation(codeLocationViewUrl, hubService.getGson().toJson(codeLocationView));
    }

    public void updateCodeLocation(final String codeLocationViewUrl, final String codeLocationViewJson) throws IntegrationException {
        final Request request = new Request.Builder(codeLocationViewUrl).method(HttpMethod.PUT).bodyContent(new StringBodyContent(codeLocationViewJson)).build();
        try (Response response = hubService.executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteCodeLocations(final List<CodeLocationView> codeLocationViews) throws IntegrationException {
        for (final CodeLocationView codeLocationView : codeLocationViews) {
            deleteCodeLocation(codeLocationView);
        }
    }

    public void deleteCodeLocation(final CodeLocationView codeLocationView) throws IntegrationException {
        final String codeLocationViewUrl = hubService.getHref(codeLocationView);
        deleteCodeLocation(codeLocationViewUrl);
    }

    public void deleteCodeLocation(final String codeLocationViewUrl) throws IntegrationException {
        final Request deleteRequest = new Request.Builder(codeLocationViewUrl).method(HttpMethod.DELETE).build();
        try (Response response = hubService.executeRequest(deleteRequest)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public CodeLocationView getCodeLocationByName(final String codeLocationName) throws IntegrationException {
        if (StringUtils.isNotBlank(codeLocationName)) {
            final Optional<HubQuery> hubQuery = HubQuery.createQuery("name", codeLocationName);
            final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(hubQuery);
            final List<CodeLocationView> codeLocations = hubService.getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, requestBuilder);
            for (final CodeLocationView codeLocation : codeLocations) {
                if (codeLocationName.equals(codeLocation.name)) {
                    return codeLocation;
                }
            }
        }

        throw new DoesNotExistException("This Code Location does not exist. Code Location: " + codeLocationName);
    }

    public CodeLocationView getCodeLocationById(final String codeLocationId) throws IntegrationException {
        final HubPath hubPath = new HubPath(ApiDiscovery.CODELOCATIONS_LINK.getPath() + "/" + codeLocationId);
        final HubPathSingleResponse<CodeLocationView> codeLocationResponse = new HubPathSingleResponse<>(hubPath, CodeLocationView.class);
        return hubService.getResponse(codeLocationResponse);
    }

    private CodeLocationView createRequestCodeLocationView(final CodeLocationView codeLocationView, final String versionUrl) {
        final CodeLocationView requestCodeLocationView = new CodeLocationView();
        requestCodeLocationView.createdAt = codeLocationView.createdAt;
        requestCodeLocationView.mappedProjectVersion = versionUrl;
        requestCodeLocationView.name = codeLocationView.name;
        requestCodeLocationView.updatedAt = codeLocationView.updatedAt;
        requestCodeLocationView.url = codeLocationView.url;
        return requestCodeLocationView;
    }

    public ScanSummaryView getScanSummaryViewById(final String scanSummaryId) throws IntegrationException {
        final String uri = HubService.SCANSUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        return hubService.getResponse(uri, ScanSummaryView.class);
    }

}
