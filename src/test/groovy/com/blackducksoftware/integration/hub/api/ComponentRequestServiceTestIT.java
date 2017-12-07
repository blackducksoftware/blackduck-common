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
package com.blackducksoftware.integration.hub.api;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.bdio.SimpleBdioFactory;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.model.response.ComponentSearchResultResponse;
import com.blackducksoftware.integration.hub.model.view.components.OriginView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class ComponentRequestServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingHubCommonWithExternalId() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final ComponentRequestService componentRequestService = hubServicesFactory.createComponentRequestService();
        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        final ExternalId hubCommonExternalId = simpleBdioFactory.createMavenExternalId("com.blackducksoftware.integration", "hub-common", "2.1.0");
        final ComponentSearchResultResponse componentItem = componentRequestService.getExactComponentMatch(hubCommonExternalId);

        assertNotNull(componentItem);
    }

    @Test
    public void testGettingHubCommonWithOriginView() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final ComponentRequestService componentRequestService = hubServicesFactory.createComponentRequestService();
        final OriginView hubCommonOriginView = new OriginView();

        hubCommonOriginView.externalNamespace = "maven";
        hubCommonOriginView.externalId = "com.blackducksoftware.integration:hub-common:2.1.0";

        final ComponentSearchResultResponse componentItem = componentRequestService.getExactComponentMatch(hubCommonOriginView);

        assertNotNull(componentItem);
    }

}
