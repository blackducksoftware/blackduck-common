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
package com.blackducksoftware.integration.hub.api.aggregate.bom;

import java.util.List;

import org.apache.commons.io.IOUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class AggregateBomService extends HubService {
    public AggregateBomService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<VersionBomComponentView> getBomEntries(final ProjectVersionView projectVersion) throws IntegrationException {
        return getAllViewsFromLinkSafely(projectVersion, MetaHandler.COMPONENTS_LINK, VersionBomComponentView.class);
    }

    public List<VersionBomComponentView> getBomEntries(final String componentsUrl) throws IntegrationException {
        return getAllViews(componentsUrl, VersionBomComponentView.class);
    }

    public void addBomComponent(final String mediaType, final String projectVersionComponentsUrl, final String componentVersionUrl) throws IntegrationException {
        Response response = null;
        try {
            final HubRequest hubRequest = getHubRequestFactory().createRequest(projectVersionComponentsUrl);
            response = hubRequest.executePost(mediaType, "{\"component\": \"" + componentVersionUrl + "\"}");
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

}
