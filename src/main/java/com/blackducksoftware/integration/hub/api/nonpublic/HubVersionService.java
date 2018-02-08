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
package com.blackducksoftware.integration.hub.api.nonpublic;

import static com.blackducksoftware.integration.hub.RestConstants.QUERY_VERSION;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.response.VersionComparison;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class HubVersionService extends HubService {

    public HubVersionService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String getHubVersion() throws IntegrationException {
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), "api/v1/current-version");
        final Request request = new Request(uri);

        try (Response response = getRestConnection().executeRequest(request)) {
            final String hubVersionWithPossibleSurroundingQuotes = response.getContentString();
            final String hubVersion = hubVersionWithPossibleSurroundingQuotes.replace("\"", "");
            return hubVersion;
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public VersionComparison getHubVersionComparison(final String consumerVersion) throws IntegrationException, IOException {
        final String uri = getHubRequestFactory().pieceTogetherURI(getHubBaseUrl(), "api/v1/current-version-comparison");
        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_VERSION, consumerVersion);
        final Request request = getHubRequestFactory().createGetRequest(uri, queryParameters);

        try (Response response = getRestConnection().executeRequest(request)) {
            final String jsonResponse = response.getContentString();
            final VersionComparison versionComparison = getGson().fromJson(jsonResponse, VersionComparison.class);
            return versionComparison;
        }
    }

    public boolean isConsumerVersionLessThanOrEqualToServerVersion(final String consumerVersion) throws IntegrationException, IOException {
        final VersionComparison versionComparison = getHubVersionComparison(consumerVersion);
        if (versionComparison.numericResult <= 0) {
            return true;
        } else {
            return false;
        }
    }

}
