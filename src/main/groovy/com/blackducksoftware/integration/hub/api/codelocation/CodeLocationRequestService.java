/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.api.codelocation;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_CODE_LOCATIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.model.enumeration.CodeLocationEnum;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class CodeLocationRequestService extends HubResponseService {
    private static final List<String> CODE_LOCATION_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_CODE_LOCATIONS);

    private final MetaService metaService;

    public CodeLocationRequestService(final RestConnection restConnection, final MetaService metaService) {
        super(restConnection);
        this.metaService = metaService;
    }

    public List<CodeLocationView> getAllCodeLocations() throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(CODE_LOCATION_SEGMENTS);
        final List<CodeLocationView> allCodeLocations = getAllItems(hubPagedRequest, CodeLocationView.class);
        return allCodeLocations;
    }

    public List<CodeLocationView> getAllCodeLocationsForCodeLocationType(final CodeLocationEnum codeLocationType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(CODE_LOCATION_SEGMENTS).addQueryParameter("codeLocationType",
                codeLocationType.toString());

        final List<CodeLocationView> allCodeLocations = getAllItems(hubPagedRequest, CodeLocationView.class);
        return allCodeLocations;
    }

    public List<CodeLocationView> getAllCodeLocationsForProjectVersion(final ProjectVersionView version) throws IntegrationException {
        final String codeLocationUrl = metaService.getFirstLink(version, MetaService.CODE_LOCATION_LINK);
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(codeLocationUrl);

        final List<CodeLocationView> allCodeLocations = getAllItems(hubPagedRequest, CodeLocationView.class);
        return allCodeLocations;
    }

    public void unmapCodeLocations(final List<CodeLocationView> codeLocationItems) throws IntegrationException {
        for (final CodeLocationView codeLocationItem : codeLocationItems) {
            unmapCodeLocation(codeLocationItem);
        }
    }

    public void unmapCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = metaService.getHref(codeLocationItem);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationItem, "");
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(requestCodeLocationView));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationItem, final ProjectVersionView version) throws IntegrationException {
        mapCodeLocation(codeLocationItem, metaService.getHref(version));
    }

    public void mapCodeLocation(final CodeLocationView codeLocationItem, final String versionUrl) throws IntegrationException {
        final String codeLocationItemUrl = metaService.getHref(codeLocationItem);
        final CodeLocationView requestCodeLocationView = createRequestCodeLocationView(codeLocationItem, versionUrl);
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(requestCodeLocationView));
    }

    public void updateCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = metaService.getHref(codeLocationItem);
        updateCodeLocation(codeLocationItemUrl, getGson().toJson(codeLocationItem));
    }

    public void updateCodeLocation(final String codeLocationItemUrl, final String codeLocationItemJson) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(codeLocationItemUrl);
        request.executePut(codeLocationItemJson);
    }

    public void deleteCodeLocations(final List<CodeLocationView> codeLocationItems) throws IntegrationException {
        for (final CodeLocationView codeLocationItem : codeLocationItems) {
            deleteCodeLocation(codeLocationItem);
        }
    }

    public void deleteCodeLocation(final CodeLocationView codeLocationItem) throws IntegrationException {
        final String codeLocationItemUrl = metaService.getHref(codeLocationItem);
        deleteCodeLocation(codeLocationItemUrl);
    }

    public void deleteCodeLocation(final String codeLocationItemUrl) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(codeLocationItemUrl);
        request.executeDelete();
    }

    public CodeLocationView getCodeLocationById(final String codeLocationId) throws IntegrationException {
        final List<String> segments = new ArrayList<>(CODE_LOCATION_SEGMENTS);
        segments.add(codeLocationId);
        final HubRequest request = getHubRequestFactory().createRequest(segments);
        return getItem(request, CodeLocationView.class);
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

}
