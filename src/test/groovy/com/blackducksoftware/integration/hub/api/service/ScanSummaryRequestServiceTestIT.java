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

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.scan.DryRunUploadRequestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRequestService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectDataService;
import com.blackducksoftware.integration.hub.dataservice.project.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.model.enumeration.CodeLocationEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.model.request.ProjectRequest;
import com.blackducksoftware.integration.hub.model.request.ProjectVersionRequest;
import com.blackducksoftware.integration.hub.model.response.DryRunUploadResponse;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.test.TestLogger;

public class ScanSummaryRequestServiceTestIT {

    private final static RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    private static final IntLogger logger = new TestLogger();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws IllegalArgumentException, IntegrationException {
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final ScanSummaryRequestService scanSummaryRequestService = hubServicesFactory.createScanSummaryRequestService();
        final CodeLocationRequestService codeLocationRequestService = hubServicesFactory.createCodeLocationRequestService(logger);
        final ProjectRequestService projectRequestService = hubServicesFactory.createProjectRequestService(logger);
        final ProjectVersionRequestService projectVersionRequestService = hubServicesFactory.createProjectVersionRequestService(logger);
        final MetaService metaService = hubServicesFactory.createMetaService(logger);
      
        try {
            final ProjectView projectItem = projectRequestService.getProjectByName(restConnectionTestHelper.getProperty("TEST_PROJECT"));
            final ProjectVersionView projectVersionItem = projectVersionRequestService.getProjectVersion(projectItem, restConnectionTestHelper.getProperty("TEST_VERSION"));
            final String projectVersionUrl = metaService.getHref(projectVersionItem);

            final List<CodeLocationView> allCodeLocations = codeLocationRequestService
                    .getAllCodeLocationsForCodeLocationType(CodeLocationEnum.BOM_IMPORT);
            assertNotNull(allCodeLocations);

            final List<String> allScanSummariesLinks = new ArrayList<>();
            for (final CodeLocationView codeLocationItem : allCodeLocations) {
                logger.debug("Checking codeLocation: " + codeLocationItem.name);
                final String mappedProjectVersionUrl = codeLocationItem.mappedProjectVersion;
                if (projectVersionUrl.equals(mappedProjectVersionUrl)) {
                    final String scanSummariesLink = metaService.getFirstLink(codeLocationItem, MetaService.SCANS_LINK);
                    allScanSummariesLinks.add(scanSummariesLink);
                }
            }
            assertNotNull(allScanSummariesLinks);

            final List<ScanSummaryView> allScanSummaries = new ArrayList<>();
            for (final String scanSummaryLink : allScanSummariesLinks) {
                allScanSummaries.addAll(scanSummaryRequestService.getAllScanSummaryItems(scanSummaryLink));
            }
        
            assertNotNull(allScanSummaries);
        
        }
        catch (Exception e){
        	fail();
        }

    }
}
