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
package com.blackducksoftware.integration.hub.api.component;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_COMPONENTS;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.response.ComponentSearchResultResponse;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class ComponentRequestService extends HubResponseService {
    private static final List<String> COMPONENT_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_COMPONENTS);

    public ComponentRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<ComponentSearchResultResponse> getAllComponents(final ExternalId externalId) throws IntegrationException {
        final String componentQuery = String.format("id:%s", StringUtils.join(externalId.getExternalIdPieces(), "|"));
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(COMPONENT_SEGMENTS, componentQuery);

        final List<ComponentSearchResultResponse> allComponents = getAllItems(hubPagedRequest, ComponentSearchResultResponse.class);
        return allComponents;
    }

    public ComponentSearchResultResponse getExactComponentMatch(final ExternalId externalId) throws IntegrationException {
        final List<ComponentSearchResultResponse> allComponents = getAllComponents(externalId);
        for (final ComponentSearchResultResponse componentItem : allComponents) {
            if (componentItem.originId != null) {
                final String matchId = externalId.createHubOriginId();
                if (componentItem.originId.equals(matchId)) {
                    return componentItem;
                }
            }
        }

        throw new HubIntegrationException("Couldn't find an exact component match.");
    }

}
