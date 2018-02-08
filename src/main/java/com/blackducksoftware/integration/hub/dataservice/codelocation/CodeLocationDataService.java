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
package com.blackducksoftware.integration.hub.dataservice.codelocation;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_BOM_IMPORT;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_SCAN_SUMMARIES;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.enumeration.CodeLocationType;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.exception.DoesNotExistException;
import com.blackducksoftware.integration.hub.request.PagedRequest;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class CodeLocationDataService extends HubService {
    public CodeLocationDataService(final RestConnection restConnection) {
        super(restConnection);
    }

    public void importBomFile(final File file) throws IntegrationException {
        importBomFile(file, "application/ld+json");
    }

    public void importBomFile(final File file, final String mediaType) throws IntegrationException {
        String jsonPayload;
        try {
            jsonPayload = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IntegrationException("Failed to import Bom file: " + file.getAbsolutePath() + " to the Hub because : " + e.getMessage(), e);
        }
        // TODO add bom-import to ApiDiscovery
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), Arrays.asList(SEGMENT_API, SEGMENT_BOM_IMPORT));
        final Request request = getHubRequestFactory().createRequest(uri, HttpMethod.POST);
        request.setBodyContent(jsonPayload);
        try (Response response = getRestConnection().executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public List<CodeLocationView> getAllCodeLocationsForCodeLocationType(final CodeLocationType codeLocationType) throws IntegrationException {
        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("codeLocationType", codeLocationType.toString());
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), ApiDiscovery.CODELOCATIONS_LINK);
        final PagedRequest pagedRequest = getHubRequestFactory().createGetPagedRequest(uri, queryParameters);

        final List<CodeLocationView> allCodeLocations = getAllResponses(pagedRequest, CodeLocationView.class);
        return allCodeLocations;
    }

    public void unmapCodeLocations(final List<CodeLocationView> codeLocationItems) throws IntegrationException {
        for (final CodeLocationView codeLocationItem : codeLocationItems) {
            unmapCodeLocation(codeLocationItem);
        }
    }

    public void unmapCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(codeLocationItem);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationItem, "");
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(requestCodeLocationView));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationItem, final ProjectVersionView version) throws IntegrationException {
        mapCodeLocation(codeLocationItem, getHref(version));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationItem, final String versionUrl) throws IntegrationException {
        final String codeLocationItemUrl = getHref(codeLocationItem);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationItem, versionUrl);
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(requestCodeLocationView));
    }

    public void updateCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(codeLocationItem);
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(codeLocationItem));
    }

    public void updateCodeLocation(final String codeLocationItemUrl, final String codeLocationItemJson) throws IntegrationException {
        final Request request = getHubRequestFactory().createRequest(codeLocationItemUrl, HttpMethod.PUT);
        request.setBodyContent(codeLocationItemJson);
        try (Response response = getRestConnection().executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }

    }

    public void deleteCodeLocations(final List<CodeLocationView> codeLocationItems) throws IntegrationException {
        for (final CodeLocationView codeLocationItem : codeLocationItems) {
            deleteCodeLocation(codeLocationItem);
        }
    }

    public void deleteCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(codeLocationItem);
        deleteCodeLocation(codeLocationItemUrl);
    }

    public void deleteCodeLocation(final String codeLocationItemUrl) throws IntegrationException {
        final Request request = getHubRequestFactory().createRequest(codeLocationItemUrl, HttpMethod.DELETE);
        try (Response response = getRestConnection().executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public CodeLocationView getCodeLocationByName(final String codeLocationName) throws IntegrationException {
        if (StringUtils.isNotBlank(codeLocationName)) {
            final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), ApiDiscovery.CODELOCATIONS_LINK);
            final PagedRequest pagedRequest = getHubRequestFactory().createGetPagedRequestWithQ(uri, "name:" + codeLocationName);
            final List<CodeLocationView> codeLocations = getAllResponses(pagedRequest, CodeLocationView.class);
            for (final CodeLocationView codeLocation : codeLocations) {
                if (codeLocationName.equals(codeLocation.name)) {
                    return codeLocation;
                }
            }
        }

        throw new DoesNotExistException("This Code Location does not exist. Code Location: " + codeLocationName);
    }

    public CodeLocationView getCodeLocationById(final String codeLocationId) throws IntegrationException {
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), ApiDiscovery.CODELOCATIONS_LINK + "/" + codeLocationId);
        final Request request = new Request(uri);
        return getResponse(request, CodeLocationView.class);
    }

    private CodeLocationView createRequestCodeLocationView(final CodeLocationView codeLocationItem, final String versionUrl) {
        final CodeLocationView requestCodeLocationView = new CodeLocationView();
        requestCodeLocationView.createdAt = codeLocationItem.createdAt;
        requestCodeLocationView.mappedProjectVersion = versionUrl;
        requestCodeLocationView.name = codeLocationItem.name;
        requestCodeLocationView.type = codeLocationItem.type;
        requestCodeLocationView.updatedAt = codeLocationItem.updatedAt;
        requestCodeLocationView.url = codeLocationItem.url;
        return requestCodeLocationView;
    }

    public ScanSummaryView getScanSummaryViewById(final String scanSummaryId) throws IntegrationException {
        // TODO add scan-summaries to ApiDiscovery
        final List<String> segments = Arrays.asList(SEGMENT_API, SEGMENT_SCAN_SUMMARIES);
        segments.add(scanSummaryId);
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), segments);
        final Request request = new Request(uri);
        return getResponse(request, ScanSummaryView.class);
    }
}
