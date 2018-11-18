/**
 * Hub Common
 * <p>
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.api.aggregate.bom;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.rest.RestConnectionTestHelper;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

@Tag("integration")
public class AggregateBomServiceTestIT {
    private final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Test
    public void testGetBomEntriesForUrl() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final HubService hubService = hubServicesFactory.createHubService();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final Optional<ProjectView> project = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        assertTrue(project.isPresent());
        final List<ProjectVersionView> projectVersions = hubService.getAllResponses(project.get(), ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.getVersionName().equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final String bomUrl = projectVersion.getFirstLink(ProjectVersionView.COMPONENTS_LINK).get();
        final List<VersionBomComponentView> bomComponents = hubService.getResponses(bomUrl, VersionBomComponentView.class, true);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if ((testComponentName.equals(comp.getComponentName()) && (testComponentVersionName.equals(comp.getComponentVersionName())))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.getUsages().get(0).toString());
    }

    @Test
    public void testGetBomEntriesForProjectVersion() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();

        final String testProjectName = restConnectionTestHelper.getProperty("TEST_PROJECT");
        final String testProjectVersionName = "BomRequestServiceTest";
        final String testComponentName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT");
        final String testComponentVersionName = restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_VERSION");

        final Optional<ProjectView> project = hubServicesFactory.createProjectService().getProjectByName(testProjectName);
        assertTrue(project.isPresent());
        final List<ProjectVersionView> projectVersions = hubServicesFactory.createHubService().getAllResponses(project.get(), ProjectView.VERSIONS_LINK_RESPONSE);
        ProjectVersionView projectVersion = null;
        for (final ProjectVersionView projectVersionCandidate : projectVersions) {
            if (projectVersionCandidate.getVersionName().equals(testProjectVersionName)) {
                projectVersion = projectVersionCandidate;
            }
        }
        assertNotNull(projectVersion);

        final List<VersionBomComponentView> bomComponents = hubServicesFactory.createHubService().getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        System.out.println("BOM size: " + bomComponents.size());

        // Look for testComponent in BOM
        VersionBomComponentView foundComp = null;
        for (final VersionBomComponentView comp : bomComponents) {
            if (testComponentName.equals(comp.getComponentName()) && (testComponentVersionName.equals(comp.getComponentVersionName()))) {
                foundComp = comp;
            }
        }
        assertNotNull(foundComp);
        assertEquals(restConnectionTestHelper.getProperty("TEST_PROJECT_COMPONENT_USAGE"), foundComp.getUsages().get(0).toString());
    }

}
