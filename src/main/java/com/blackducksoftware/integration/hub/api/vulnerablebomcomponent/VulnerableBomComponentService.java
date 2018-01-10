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
package com.blackducksoftware.integration.hub.api.vulnerablebomcomponent;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.HubMediaTypes;
import com.blackducksoftware.integration.hub.model.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class VulnerableBomComponentService extends HubService {
    public VulnerableBomComponentService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<VulnerableComponentView> getVulnerableComponentsMatchingComponentName(final String vulnerableBomComponentsUrl, final String componentName) throws IntegrationException {
        final List<VulnerableComponentView> allItems = getAllViews(vulnerableBomComponentsUrl, VulnerableComponentView.class, 
                HubMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        return allItems;
    }

    public List<VulnerableComponentView> getVulnerableComponentsMatchingComponentName(final String vulnerableBomComponentsUrl) throws IntegrationException {
        final List<VulnerableComponentView> allItems = getAllViews(vulnerableBomComponentsUrl, VulnerableComponentView.class, 
                HubMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        return allItems;
    }

    public List<VulnerableComponentView> getVulnerableComponentsMatchingComponentName(final String vulnerableBomComponentsUrl, final int itemsPerPage) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(itemsPerPage, vulnerableBomComponentsUrl);

        final List<VulnerableComponentView> allItems = getAllViews(hubPagedRequest, VulnerableComponentView.class, 
                HubMediaTypes.VULNERABILITY_REQUEST_SERVICE_V1);
        return allItems;
    }

}
