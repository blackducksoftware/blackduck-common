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
package com.blackducksoftware.integration.hub.buildtool;

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.BDIO_FILE_MEDIA_TYPE;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.UPLOAD_FILE_MESSAGE;

import java.io.File;
import java.io.IOException;

import com.blackducksoftware.integration.hub.api.bom.BomImportRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.buildtool.bdio.BdioDependencyWriter;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservice.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;

public class BuildToolHelper {
    private final IntLogger logger;

    public BuildToolHelper(final IntLogger logger) {
        this.logger = logger;
    }

    public void createFlatOutput(final DependencyNode rootNode, final String hubProjectName, final String hubProjectVersion,
            final File outputDirectory) throws IOException {
        final FlatDependencyListWriter flatDependencyListWriter = new FlatDependencyListWriter();
        flatDependencyListWriter.write(outputDirectory, hubProjectName, rootNode);

        final HubProjectDetailsWriter hubProjectDetailsWriter = new HubProjectDetailsWriter();
        hubProjectDetailsWriter.write(outputDirectory, hubProjectName, hubProjectVersion);
    }

    public void createHubOutput(final DependencyNode rootNode, final String projectName, final String hubProjectName, final String hubProjectVersion,
            final File outputDirectory) throws IOException {
        final BdioDependencyWriter bdioDependencyWriter = new BdioDependencyWriter();
        bdioDependencyWriter.write(outputDirectory, projectName, hubProjectName, rootNode);

        final HubProjectDetailsWriter hubProjectDetailsWriter = new HubProjectDetailsWriter();
        hubProjectDetailsWriter.write(outputDirectory, hubProjectName, hubProjectVersion);
    }

    public void deployHubOutput(final HubServicesFactory services,
            final File outputDirectory, final String hubProjectName) throws HubIntegrationException {
        final String filename = BdioDependencyWriter.getFilename(hubProjectName);
        final File file = new File(outputDirectory, filename);
        final BomImportRequestService bomImportRequestService = services.createBomImportRequestService();
        bomImportRequestService.importBomFile(file, BDIO_FILE_MEDIA_TYPE);

        logger.info(String.format(UPLOAD_FILE_MESSAGE, file, bomImportRequestService.getRestConnection().getBaseUrl()));
    }

    public void waitForHub(final HubServicesFactory services, final String hubProjectName,
            final String hubProjectVersion, final long timeoutInSeconds) throws HubIntegrationException {
        final ScanStatusDataService scanStatusDataService = services.createScanStatusDataService(logger, timeoutInSeconds * 1000);
        scanStatusDataService.assertBomImportScanStartedThenFinished(hubProjectName, hubProjectVersion);
    }

    public void createRiskReport(final HubServicesFactory services,
            final File outputDirectory, final String projectName, final String projectVersionName, final long timeoutInSeconds)
            throws HubIntegrationException {
        final RiskReportDataService reportDataService = services.createRiskReportDataService(logger, timeoutInSeconds * 1000);
        reportDataService.createRiskReportFiles(outputDirectory, projectName, projectVersionName);
    }

    public PolicyStatusItem checkPolicies(final HubServicesFactory services, final String hubProjectName,
            final String hubProjectVersion) throws HubIntegrationException {
        final PolicyStatusDataService policyStatusDataService = services.createPolicyStatusDataService(logger);
        final PolicyStatusItem policyStatusItem = policyStatusDataService
                .getPolicyStatusForProjectAndVersion(hubProjectName, hubProjectVersion);
        return policyStatusItem;
    }
}
