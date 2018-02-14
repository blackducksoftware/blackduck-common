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
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.IntegrationTest;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentSearchResultView;
import com.blackducksoftware.integration.hub.bdio.SimpleBdioFactory;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

@Category(IntegrationTest.class)
public class ComponentRequestServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingHubCommon() throws Exception {
        final HubServicesFactory hubDataServicesFactory = restConnectionTestHelper.createHubDataServicesFactory();
        final ComponentService componentRequestService = hubDataServicesFactory.createComponentDataService();
        final SimpleBdioFactory simpleBdioFactory = new SimpleBdioFactory();

        final ExternalId hubCommonExternalId = simpleBdioFactory.createMavenExternalId("com.blackducksoftware.integration", "hub-common", "2.1.0");
        final ComponentSearchResultView componentView = componentRequestService.getExactComponentMatch(hubCommonExternalId);

        assertNotNull(componentView);
    }

}
