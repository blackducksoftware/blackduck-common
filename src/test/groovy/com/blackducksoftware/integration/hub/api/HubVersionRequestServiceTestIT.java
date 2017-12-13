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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionService;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class HubVersionRequestServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGettingHubVersion() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final HubVersionService hubVersionRequestService = hubServicesFactory.createHubVersionService();
        final String hubVersion = hubVersionRequestService.getHubVersion();
        assertNotNull(hubVersion);
        System.out.println(hubVersion);
    }

    @Test
    public void testComparingHubVersions() throws Exception {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final HubVersionService hubVersionRequestService = hubServicesFactory.createHubVersionService();
        final boolean versionIsOlder = hubVersionRequestService.isConsumerVersionLessThanOrEqualToServerVersion("3.3.0");
        assertTrue(versionIsOlder);

        final boolean versionIsNewer = hubVersionRequestService.isConsumerVersionLessThanOrEqualToServerVersion("1000");
        assertFalse(versionIsNewer);
    }

}
