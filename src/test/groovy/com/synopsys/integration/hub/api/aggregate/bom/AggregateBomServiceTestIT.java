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
package com.synopsys.integration.hub.api.aggregate.bom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.api.generated.view.ProjectVersionView;
import com.synopsys.integration.hub.api.generated.view.ProjectView;
import com.synopsys.integration.hub.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.hub.api.view.MetaHandler;
import com.synopsys.integration.hub.rest.RestConnectionTestHelper;
import com.synopsys.integration.hub.service.HubService;
import com.synopsys.integration.hub.service.HubServicesFactory;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category(IntegrationTest.class)
public class AggregateBomServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGetBomEntriesForUrl() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final MetaHandler metaHandler = new MetaHandler(restConnectionTestHelper.createIntLogger());
        final HubService hubService = hubServicesFactory.createHubService();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final ProjectView project = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        final List<ProjectVersionView> projectVersions = hubService.getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.versionName.equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final String bomUrl = metaHandler.getFirstLink(projectVersion, "components");
        final List<VersionBomComponentView> bomComponents = hubService.getResponses(bomUrl, VersionBomComponentView.class, true);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if ((testComponentName.equals(comp.componentName) && (testComponentVersionName.equals(comp.componentVersionName)))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.usages.get(0).toString());
    }

    @Test
    public void testGetBomEntriesForProjectVersion() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final ProjectView project = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        final List<ProjectVersionView> projectVersions = hubServicesFactory.createHubService().getAllResponses(project, ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.versionName.equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final List<VersionBomComponentView> bomComponents = hubServicesFactory.createHubService().getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if (testComponentName.equals(comp.componentName) && (testComponentVersionName.equals(comp.componentVersionName))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.usages.get(0).toString());
    }

}
