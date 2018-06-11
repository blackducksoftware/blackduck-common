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
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;
import com.blackducksoftware.integration.hub.api.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.cli.CLIDownloadUtility;
import com.blackducksoftware.integration.hub.cli.ScanServiceOutput;
import com.blackducksoftware.integration.hub.cli.SimpleScanUtility;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
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

    public ScanServiceOutput executeScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final boolean cleanupLogsOnSuccess, final ProjectRequest projectRequest)
            throws InterruptedException, IntegrationException {
        preScan(hubServerConfig, hubScanConfig, projectRequest);
        final SimpleScanUtility simpleScanUtility = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final List<File> scanSummaryFiles = runScan(simpleScanUtility);
        List<ScanSummaryView> scanSummaryViews = postScan(hubScanConfig, cleanupLogsOnSuccess, scanSummaryFiles, projectRequest, simpleScanUtility);
        ScanServiceOutput scanServiceOutput = new ScanServiceOutput(simpleScanUtility.getLogDirectory(), simpleScanUtility.getCLILogDirectory(),
                simpleScanUtility.getStandardOutputFile(), simpleScanUtility.getDryRunFiles(), scanSummaryViews, projectVersionWrapper);
        return scanServiceOutput;
    }

    private SimpleScanUtility createScanService(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) {
        if (hubScanConfig.isDryRun()) {
            return new SimpleScanUtility(logger, hubService.getGson(), hubServerConfig, intEnvironmentVariables, hubScanConfig, projectRequest.name, projectRequest.versionRequest.versionName);
        } else {
            return new SimpleScanUtility(logger, hubService.getGson(), hubServerConfig, intEnvironmentVariables, hubScanConfig, null, null);
        }
    }

    /**
     * This should only be invoked directly when dryRun == true. Otherwise, installAndRunControlledScan should be used.
     */
    public List<File> executeDryRunScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final boolean cleanupLogsOnSuccess, final ProjectRequest projectRequest) throws InterruptedException, IntegrationException {
        final SimpleScanUtility simpleScanUtility = createScanService(hubServerConfig, hubScanConfig, projectRequest);
        final List<File> scanSummaryFiles = runScan(simpleScanUtility);
        if (cleanupLogsOnSuccess) {
            cleanUpLogFiles(simpleScanUtility);
        }
        return scanSummaryFiles;
    }

    private List<File> runScan(final SimpleScanUtility simpleScanUtility) throws IllegalArgumentException, EncryptionException, InterruptedException, HubIntegrationException {
        simpleScanUtility.setupAndExecuteScan();
        final List<File> scanSummaryFiles = simpleScanUtility.getScanSummaryFiles();
        return scanSummaryFiles;
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

    private void preScan(final HubServerConfig hubServerConfig, final HubScanConfig hubScanConfig, final ProjectRequest projectRequest) throws IntegrationException {
        printConfiguration(hubScanConfig, projectRequest);
        final CurrentVersionView currentVersion = hubService.getResponse(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        cliDownloadService.performInstallation(hubScanConfig.getToolsDir(), hubServerConfig.getHubUrl().toString(), currentVersion.version);

        if (!hubScanConfig.isDryRun()) {
            projectVersionWrapper = projectDataService.getProjectVersionAndCreateIfNeeded(projectRequest);
        }
    }

    private List<ScanSummaryView> postScan(final HubScanConfig hubScanConfig, final boolean cleanupLogsOnSuccess, List<File> scanSummaryFiles, final ProjectRequest projectRequest, final SimpleScanUtility simpleScanUtility)
            throws IntegrationException {
        logger.trace(String.format("Scan is dry run %s", hubScanConfig.isDryRun()));
        if (cleanupLogsOnSuccess) {
            cleanUpLogFiles(simpleScanUtility);
        }

        if (!hubScanConfig.isDryRun()) {
            final List<CodeLocationView> codeLocationViews = new ArrayList<>();
            final List<ScanSummaryView> scanSummaries = new ArrayList<>();
            logger.trace(String.format("Found %s scan summary files", scanSummaryFiles.size()));
            for (final File scanSummaryFile : scanSummaryFiles) {
                final ScanSummaryView scanSummary;
                try {
                    scanSummary = getScanSummaryFromFile(scanSummaryFile);
                    scanSummaries.add(scanSummary);
                    scanSummaryFile.delete();

                    // TODO update when ScanSummaryView is part of the swagger
                    final String codeLocationUrl = hubService.getFirstLinkSafely(scanSummary, ScanSummaryView.CODELOCATION_LINK);

                    final CodeLocationView codeLocationView = hubService.getResponse(codeLocationUrl, CodeLocationView.class);
                    codeLocationViews.add(codeLocationView);
                    codeLocationDataService.mapCodeLocation(codeLocationView, projectVersionWrapper.getProjectVersionView());
                } catch (final IOException ex) {
                    logger.trace("Error reading scan summary file", ex);
                }
            }
            simpleScanUtility.getStatusDirectory().delete();
            return scanSummaries;
        }
        return Collections.emptyList();
    }

    private ScanSummaryView getScanSummaryFromFile(final File scanSummaryFile) throws IOException {
        final String scanSummaryJson = FileUtils.readFileToString(scanSummaryFile, Charset.forName("UTF8"));
        final ScanSummaryView scanSummaryView = hubService.getGson().fromJson(scanSummaryJson, ScanSummaryView.class);
        scanSummaryView.json = scanSummaryJson;
        return scanSummaryView;
    }

    private void cleanUpLogFiles(final SimpleScanUtility simpleScanUtility) {
        final File standardOutputFile = simpleScanUtility.getStandardOutputFile();
        if (standardOutputFile != null && standardOutputFile.exists()) {
            standardOutputFile.delete();
        }
        final File cliLogDirectory = simpleScanUtility.getCLILogDirectory();
        if (cliLogDirectory != null && cliLogDirectory.exists()) {
            for (final File log : cliLogDirectory.listFiles()) {
                log.delete();
            }
            cliLogDirectory.delete();
        }
    }

}
