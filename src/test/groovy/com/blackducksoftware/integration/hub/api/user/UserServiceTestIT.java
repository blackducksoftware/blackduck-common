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
package com.blackducksoftware.integration.hub.api.user;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.user.UserDataService;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.RoleView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.rest.TestingPropertyKey;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class UserServiceTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper(TestingPropertyKey.TEST_HTTPS_HUB_SERVER_URL.toString());

    // TODO - Tested in-house; we need a dedicated Hub 4.3.x instance for testing before we can uncomment this.
    // @Test
    public void getProjectsForUserTestIT() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final UserDataService userDS = hubServicesFactory.createUserDataService();

        final List<ProjectView> projectsForUser = userDS.getProjectsForUser(restConnectionTestHelper.getTestUsername());
        assertNotNull(projectsForUser);
    }

    @Test
    public void getRolesForUserTestIT() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final UserDataService userDS = hubServicesFactory.createUserDataService();

        final List<RoleView> rolesForUser = userDS.getRolesForUser(restConnectionTestHelper.getTestUsername());
        assertTrue(rolesForUser.size() > 0);
    }
}
