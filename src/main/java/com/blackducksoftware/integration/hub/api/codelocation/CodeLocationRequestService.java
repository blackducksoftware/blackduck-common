/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.codelocation;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_CODE_LOCATIONS;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubParameterizedRequestService;

public class CodeLocationRequestService extends HubParameterizedRequestService<CodeLocationItem> {
    private static final List<String> CODE_LOCATION_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_CODE_LOCATIONS);

    public CodeLocationRequestService(final RestConnection restConnection) {
        super(restConnection, CodeLocationItem.class);
    }

    public List<CodeLocationItem> getAllCodeLocations() throws HubIntegrationException {
        final List<CodeLocationItem> allCodeLocations = getAllItems(CODE_LOCATION_SEGMENTS);
        return allCodeLocations;
    }

    public List<CodeLocationItem> getAllCodeLocationsForCodeLocationType(final CodeLocationTypeEnum codeLocationType) throws HubIntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createGetPagedRequest(CODE_LOCATION_SEGMENTS).addQueryParameter("codeLocationType",
                codeLocationType.toString());

        final List<CodeLocationItem> allCodeLocations = getAllItems(hubPagedRequest);
        return allCodeLocations;
    }

}
