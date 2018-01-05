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
package com.blackducksoftware.integration.hub.api.license;

import org.apache.commons.io.IOUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.model.view.LicenseView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class LicenseService extends HubService {
    public LicenseService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String getLicenseText(final LicenseView licenseView) throws IntegrationException {
        final String licenseTextUrl = getFirstLinkSafely(licenseView, MetaHandler.TEXT_LINK);
        final HubRequest hubRequest = getHubRequestFactory().createRequest(licenseTextUrl);
        Response response = null;
        try {
            response = hubRequest.executeGet();
            final String jsonResponse = readResponseString(response);
            return jsonResponse;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }
}
