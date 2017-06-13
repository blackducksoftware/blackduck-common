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
package com.blackducksoftware.integration.hub.api.vulnerablebomcomponent;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class VulnerableBomComponentRequestService extends HubResponseService {
    public VulnerableBomComponentRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<VulnerableComponentView> getVulnerableComponentsMatchingComponentName(
            final String vulnerableBomComponentsUrl, final String componentName) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(vulnerableBomComponentsUrl, componentName);

        final List<VulnerableComponentView> allItems = getAllItems(hubPagedRequest, VulnerableComponentView.class);
        return allItems;
    }

    public List<VulnerableComponentView> getVulnerableComponentsMatchingComponentName(
            final String vulnerableBomComponentsUrl) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(vulnerableBomComponentsUrl);

        final List<VulnerableComponentView> allItems = getAllItems(hubPagedRequest, VulnerableComponentView.class);
        return allItems;
    }
}
