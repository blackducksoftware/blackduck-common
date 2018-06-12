/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.cli.ScanServiceOutput;
import com.blackducksoftware.integration.hub.cli.SignatureScanConfig;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;

public class SignatureScannerService extends DataService {
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final CLIDownloadUtility cliDownloadService;
    private final ProjectService projectDataService;
    private final CodeLocationService codeLocationDataService;

    private ProjectVersionWrapper projectVersionWrapper;

    public SignatureScannerService(final HubService hubService, final IntEnvironmentVariables intEnvironmentVariables, final CLIDownloadUtility cliDownloadService, final ProjectService projectDataService,
            final CodeLocationService codeLocationDataService) {
        super(hubService);
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.cliDownloadService = cliDownloadService;
        this.projectDataService = projectDataService;
        this.codeLocationDataService = codeLocationDataService;
    }

    public List<ScanServiceOutput> executeScans(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest)
            throws InterruptedException, IntegrationException {
        return executeScans(hubServerConfig, hubScanConfig, projectRequest, null);
    }

    public List<ScanServiceOutput> executeScans(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, File signatureScanDirectory)
            throws InterruptedException, IntegrationException {
        CLILocation cliLocation = preScan(hubServerConfig, hubScanConfig, projectRequest, signatureScanDirectory);
        final List<SimpleScanUtility> simpleScanUtilities = createScanUtilities(hubServerConfig, hubScanConfig, projectRequest);

        List<ScanServiceOutput> scanServiceOutputs = new ArrayList<>();

        logger.info("Starting the Hub signature scans");
        for (SimpleScanUtility simpleScanUtility : simpleScanUtilities) {
            ScanSummaryView scanSummaryView = null;
            File standardOutputFile = null;
            File cliLogDirectory = null;
            String scanTarget = simpleScanUtility.getSignatureScanConfig().getScanTarget();
            try {
                simpleScanUtility.setupAndExecuteScan(cliLocation);

                scanSummaryView = getScanSummaryFromFile(simpleScanUtility.getScanSummaryFile());
                standardOutputFile = simpleScanUtility.getStandardOutputFile();
                cliLogDirectory = simpleScanUtility.getCLILogDirectory();
            } catch (IllegalArgumentException | IntegrationException e) {
                String errorMessage = String.format("There was a problem scanning target '%s' : %s", scanTarget, e.getMessage());
                ScanServiceOutput scanServiceOutput = ScanServiceOutput.FAILURE(scanTarget, simpleScanUtility.getLogDirectory(), cliLogDirectory,
                        standardOutputFile, simpleScanUtility.getDryRunFile(), scanSummaryView, projectVersionWrapper, errorMessage, e);
                scanServiceOutputs.add(scanServiceOutput);
                continue;
            }

            logger.info(String.format("Starting the post scan step for target %s", simpleScanUtility.getSignatureScanConfig().getScanTarget()));
            try {
                postScan(hubScanConfig, scanSummaryView, standardOutputFile, cliLogDirectory);
            } catch (IntegrationException e) {
                String errorMessage = String.format("There was a problem mapping the code location for scan target '%s' : %s", scanTarget, e.getMessage());
                ScanServiceOutput scanServiceOutput = ScanServiceOutput.FAILURE(scanTarget, simpleScanUtility.getLogDirectory(), cliLogDirectory,
                        standardOutputFile, simpleScanUtility.getDryRunFile(), scanSummaryView, projectVersionWrapper, errorMessage, e);
                scanServiceOutputs.add(scanServiceOutput);
                continue;
            }
            logger.info(String.format("Completed the post scan step for target %s", simpleScanUtility.getSignatureScanConfig().getScanTarget()));

            ScanServiceOutput scanServiceOutput = ScanServiceOutput.SUCCESS(scanTarget, simpleScanUtility.getLogDirectory(), cliLogDirectory,
                    standardOutputFile, simpleScanUtility.getDryRunFile(), scanSummaryView, projectVersionWrapper);
            scanServiceOutputs.add(scanServiceOutput);

        }
        logger.info("Completed the Hub signature scans");
        return scanServiceOutputs;
    }

    private List<SimpleScanUtility> createScanUtilities(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        List<SimpleScanUtility> simpleScanUtilities = new ArrayList<>();
        for (SignatureScanConfig signatureScanConfig : hubScanConfig.createSignatureScanConfigs()) {
            String projectName = null;
            String projectVersionName = null;
            if (hubScanConfig.isDryRun()) {
                projectName = projectRequest.name;
                projectVersionName = projectRequest.versionRequest.versionName;
            }
            simpleScanUtilities.add(new SimpleScanUtility(logger, hubService.getGson(), hubServerConfig, intEnvironmentVariables, signatureScanConfig, projectName, projectVersionName));
        }
        return simpleScanUtilities;
    }

    private void printConfiguration(final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        logger.alwaysLog(String.format("--> Log Level : %s", logger.getLogLevel().name()));
        String projectName = null;
        String projectVersionName = null;
        String projectVersionPhase = null;
        String projectVersionDistribution = null;
        if (projectRequest != null) {
            projectName = projectRequest.name;
            if (projectRequest.versionRequest != null) {
                projectVersionName = projectRequest.versionRequest.versionName;
                projectVersionPhase = projectRequest.versionRequest.phase == null ? null : projectRequest.versionRequest.phase.toString();
                projectVersionDistribution = projectRequest.versionRequest.distribution == null ? null : projectRequest.versionRequest.distribution.toString();
            }
        }
        logger.alwaysLog(String.format("--> Using Hub Project Name : %s, Version : %s, Phase : %s, Distribution : %s", projectName, projectVersionName, projectVersionPhase, projectVersionDistribution));
        hubScanConfig.print(logger);
    }

    private CLILocation preScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest, File signatureScanDirectory) throws IntegrationException {
        printConfiguration(hubScanConfig, projectRequest);
        final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        File directoryToInstallTo;
        if (null != signatureScanDirectory) {
            directoryToInstallTo = signatureScanDirectory;
        } else {
            directoryToInstallTo = hubScanConfig.getToolsDir();
        }
        CLILocation cliLocation = cliDownloadService.performInstallation(directoryToInstallTo, hubServerConfig.getHubUrl().toString(), currentVersion.version);

        if (!hubScanConfig.isDryRun()) {
            projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectRequest);
        }
        return cliLocation;
    }

    private void postScan(final HubScanConfig hubScanConfig, ScanSummaryView scanSummaryView, File standardOutputFiles, File cliLogDirectories)
            throws IntegrationException {
        logger.trace(String.format("Scan is dry run %s", hubScanConfig.isDryRun()));
        if (hubScanConfig.isCleanupLogsOnSuccess()) {
            cleanUpLogFiles(standardOutputFiles, cliLogDirectories);
        }

        if (!hubScanConfig.isDryRun()) {
            // TODO update when ScanSummaryView is part of the swagger
            final String codeLocationUrl = hubService.getFirstLinkSafely(scanSummaryView, ScanSummaryView.CODELOCATION_LINK);

            final CodeLocationView codeLocationView = hubService.getResponse(codeLocationUrl, CodeLocationView.class);
            codeLocationDataService.mapCodeLocation(codeLocationView, projectVersionWrapper.getProjectVersionView());
        }
    }

    private ScanSummaryView getScanSummaryFromFile(final File scanSummaryFile) {
        ScanSummaryView scanSummaryView = null;
        try {
            if (null != scanSummaryFile) {
                final String scanSummaryJson = FileUtils.readFileToString(scanSummaryFile, Charset.forName("UTF8"));
                scanSummaryView = hubService.getGson().fromJson(scanSummaryJson, ScanSummaryView.class);
                scanSummaryView.json = scanSummaryJson;
                scanSummaryFile.delete();
                scanSummaryFile.getParentFile().delete();
            }
        } catch (final IOException ex) {
            logger.trace("Error reading scan summary file", ex);
        }
        return scanSummaryView;
    }

    private void cleanUpLogFiles(final File standardOutputFile, File cliLogDirectory) {
        if (standardOutputFile != null && standardOutputFile.exists()) {
            standardOutputFile.delete();
        }
        if (cliLogDirectory != null && cliLogDirectory.exists()) {
            for (final File log : cliLogDirectory.listFiles()) {
                log.delete();
            }
            cliLogDirectory.delete();
        }

    }

}
