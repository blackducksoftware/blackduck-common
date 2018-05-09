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
package com.blackducksoftware.integration.hub.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubPath;
import com.blackducksoftware.integration.hub.api.core.HubPathSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.enumeration.CodeLocationType;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.request.BodyContent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.service.model.HubQuery;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;

public class CodeLocationService extends DataService {
    public CodeLocationService(final HubService hubService) {
        super(hubService);
    }

    public void importBomFile(final File file) throws IntegrationException {
        importBomFile(file, "application/ld+json");
    }

    public void importBomFile(final File file, final String mimeType) throws IntegrationException {
        String jsonPayload;
        try {
            jsonPayload = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IntegrationException("Failed to import Bom file: " + file.getAbsolutePath() + " to the Hub because : " + e.getMessage(), e);
        }

        final String uri = hubService.getUri(HubService.BOMIMPORT_PATH);
        final Request request = RequestFactory.createCommonPostRequestBuilder(jsonPayload).uri(uri).mimeType(mimeType).build();
        try (Response response = hubService.executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public List<CodeLocationView> getAllCodeLocationsForCodeLocationType(final CodeLocationType codeLocationType) throws IntegrationException {
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder().addQueryParameter("codeLocationType", codeLocationType.toString());
        final List<CodeLocationView> allCodeLocations = hubService.getAllResponses(ApiDiscovery.CODELOCATIONS_LINK_RESPONSE, requestBuilder);
        return allCodeLocations;
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
        final Request request = new Request.Builder(codeLocationViewUrl).method(HttpMethod.PUT).bodyContent(new BodyContent(codeLocationViewJson)).build();
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
            final HubQuery hubQuery = new HubQuery("name:" + codeLocationName);
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
        requestCodeLocationView.type = codeLocationView.type;
        requestCodeLocationView.updatedAt = codeLocationView.updatedAt;
        requestCodeLocationView.url = codeLocationView.url;
        return requestCodeLocationView;
    }

    public ScanSummaryView getScanSummaryViewById(final String scanSummaryId) throws IntegrationException {
        final String uri = HubService.SCANSUMMARIES_PATH.getPath() + "/" + scanSummaryId;
        return hubService.getResponse(uri, ScanSummaryView.class);
    }

}
