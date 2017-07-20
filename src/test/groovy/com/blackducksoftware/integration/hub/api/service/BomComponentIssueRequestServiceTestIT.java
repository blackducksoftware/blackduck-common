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
package com.blackducksoftware.integration.hub.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.aggregate.bom.AggregateBomRequestService;
import com.blackducksoftware.integration.hub.api.bom.BomComponentIssueRequestService;
import com.blackducksoftware.integration.hub.api.component.ComponentRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.model.view.IssueView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubResponseService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.test.TestLogger;

public class BomComponentIssueRequestServiceTestIT {

    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper("TEST_HUB_SERVER_URL");

    private static final IntLogger logger = new TestLogger();

    private static final String projectName = restConnectionTestHelper.getProperty("TEST_PROJECT");

    private static final String projectVersionName = restConnectionTestHelper.getProperty("TEST_VERSION");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {
        // TODO: Version 3.7+ needed for "component-issues"

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final BomComponentIssueRequestService bomComponentIssueRequestService = hubServicesFactory.createBomComponentIssueRequestService(logger);
        final MetaService metaService = hubServicesFactory.createMetaService(logger);
        final ComponentRequestService componentRequestService = hubServicesFactory.createComponentRequestService();
        final AggregateBomRequestService aggregateBomRequestService = hubServicesFactory.createAggregateBomRequestService(logger);

        final ProjectDataService projectDataService = hubServicesFactory.createProjectDataService(logger);
        final ProjectVersionWrapper projectVersionWrapper = projectDataService.getProjectVersion(restConnectionTestHelper.getProperty("TEST_PROJECT"), restConnectionTestHelper.getProperty("TEST_VERSION"));

        final ProjectView project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();

        final String componentURL = metaService.getFirstLink(projectVersion, MetaService.COMPONENTS_LINK);

        final HubResponseService componentResponseService = new HubResponseService(hubServicesFactory.getRestConnection());
        final List<VersionBomComponentView> bomComponentViews = aggregateBomRequestService.getBomEntries(projectVersion);
        final VersionBomComponentView bomComponentView = bomComponentViews.get(0);
        final String issuesURL = metaService.getFirstLink(bomComponentView, MetaService.COMPONENT_ISSUES);

        final IssueView issueView = new IssueView();
        issueView.issueDescription = "IT Service Test Description";
        issueView.issueId = "IT Service Test ID";
        issueView.issueAssignee = "IT Service Test Assignee";
        issueView.issueLink = "IT Service Test Link";
        issueView.issueStatus = "IT Service Test Status";
        issueView.issueCreatedAt = "IT Service Test CreatedAt";
        issueView.issueUpdatedAt = "IT Service Test UpdatedAt";

        final String issueItemURL = bomComponentIssueRequestService.createIssue(issueView, issuesURL);
        assertNotNull(issueItemURL);

        try {
            bomComponentIssueRequestService.deleteIssue(issueItemURL);
        } catch (final IntegrationException e) {
            fail("could not find issue to delete");
        }

    }

}
