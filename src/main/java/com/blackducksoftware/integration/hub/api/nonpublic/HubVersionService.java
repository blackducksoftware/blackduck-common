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
package com.blackducksoftware.integration.hub.api.nonpublic;

import static com.blackducksoftware.integration.hub.RestConstants.QUERY_VERSION;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_CURRENT_VERSION;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_CURRENT_VERSION_COMPARISON;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.response.VersionComparison;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class HubVersionService extends HubService {
    private static final List<String> CURRENT_VERSION_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_CURRENT_VERSION);
    private static final List<String> CURRENT_VERSION_COMPARISON_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_CURRENT_VERSION_COMPARISON);

    public HubVersionService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String getHubVersion() throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(CURRENT_VERSION_SEGMENTS);
        Response response = null;
        try {
            response = request.executeGet();
            final String hubVersionWithPossibleSurroundingQuotes = readResponseString(response);
            final String hubVersion = hubVersionWithPossibleSurroundingQuotes.replace("\"", "");
            return hubVersion;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    public VersionComparison getHubVersionComparison(final String consumerVersion) throws IntegrationException {
        final HubRequest hubVersionRequest = getHubRequestFactory().createRequest(CURRENT_VERSION_COMPARISON_SEGMENTS).addQueryParameter(QUERY_VERSION, consumerVersion);
        Response response = null;
        try {
            response = hubVersionRequest.executeGet();
            final String jsonResponse = readResponseString(response);
            final VersionComparison versionComparison = getGson().fromJson(jsonResponse, VersionComparison.class);
            return versionComparison;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    public boolean isConsumerVersionLessThanOrEqualToServerVersion(final String consumerVersion) throws IntegrationException {
        final VersionComparison versionComparison = getHubVersionComparison(consumerVersion);
        if (versionComparison.numericResult <= 0) {
            return true;
        } else {
            return false;
        }
    }

}
